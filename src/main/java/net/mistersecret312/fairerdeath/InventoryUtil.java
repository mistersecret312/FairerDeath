package net.mistersecret312.fairerdeath;

import java.util.*;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.mistersecret312.fairerdeath.InventoryStorageAttachment.ItemKey;

public class InventoryUtil
{
	public static List<InventoryStorageAttachment.Item> extractItems(ServerPlayer player, Modes mode, Set<Categories> categories, List<ItemStack> droppedItems)
	{
		List<InventoryStorageAttachment.Item> saved = new ArrayList<>();
		Inventory inv = player.getInventory();
		Random random = new Random();

		if (mode == Modes.OLD)
		{
			InventoryStorageAttachment tracker = player.getData(AttachmentTypeInit.STORAGE);
			Map<ItemKey, Integer> quotas = tracker.getKeepQuotas(player.level().getGameTime(), Config.AGE_TICKS.get());

			for (int i = 0; i < inv.getContainerSize(); i++) 
			{
				ItemStack stack = inv.getItem(i);
				if (stack.isEmpty())
					continue;

				ItemKey key = ItemKey.from(stack);
				int quota = quotas.getOrDefault(key, 0);

				if (quota > 0)
				{
					int amountToKeep = Math.min(stack.getCount(), quota);

					ItemStack keptStack = stack.copy();
					keptStack.setCount(amountToKeep);
					saved.add(new InventoryStorageAttachment.Item(i, keptStack));

					stack.shrink(amountToKeep);
					if (stack.isEmpty())
						inv.setItem(i, ItemStack.EMPTY);

					quotas.put(key, quota - amountToKeep);
				}

				if (!stack.isEmpty())
					droppedItems.add(stack.copy());
			}
			return saved;
		}

		for (int i = 0; i < inv.getContainerSize(); i++)
		{
			ItemStack stack = inv.getItem(i);
			if (stack.isEmpty())
				continue;

			boolean keep = false;
			switch (mode)
			{
				case TAG -> keep = stack.is(FairerDeath.KEEP_AT_DEATH);
				case RANDOM -> keep = random.nextFloat() < Config.RANDOM_CHANCE.get();
				case CATEGORIES -> keep = keepByCategory(i, categories);
				case FULL -> keep = true;
				case null, default -> keep = false;
			}

			if (keep)
			{
				saved.add(new InventoryStorageAttachment.Item(i, stack.copy()));
				inv.setItem(i, ItemStack.EMPTY);
			}
			else droppedItems.add(stack.copy());
		}
		return saved;
	}

	public static List<InventoryStorageAttachment.Item> extractItems(ServerPlayer player, Collection<ItemEntity> drops, Modes mode, Set<Categories> categories, List<ItemStack> dropped)
	{
		List<InventoryStorageAttachment.Item> saved = new ArrayList<>();
		Random random = new Random();
		Iterator<ItemEntity> iterator = drops.iterator();

		if (mode == Modes.OLD) 
		{
			InventoryStorageAttachment storage = player.getData(AttachmentTypeInit.STORAGE);
			Map<ItemKey, Integer> quotas = storage.getKeepQuotas(player.level().getGameTime(), Config.AGE_TICKS.get());

			while (iterator.hasNext())
			{
				ItemEntity entity = iterator.next();
				ItemStack stack = entity.getItem();

				boolean isDropped = false;
				for (int i = 0; i < dropped.size(); i++)
				{
					ItemStack rejected = dropped.get(i);

					if (ItemStack.isSameItemSameComponents(stack, rejected) && stack.getCount() == rejected.getCount())
					{
						dropped.remove(i);
						isDropped = true;
						break;
					}
				}

				if (isDropped)
					continue;

				ItemKey key = ItemKey.from(stack);
				int quota = quotas.getOrDefault(key, 0);

				if (quota > 0)
				{
					if (stack.getCount() <= quota)
					{
						saved.add(new InventoryStorageAttachment.Item(-1, stack.copy()));
						quotas.put(key, quota - stack.getCount());
						iterator.remove();
					}
					else
					{
						ItemStack keptStack = stack.copy();
						keptStack.setCount(quota);
						saved.add(new InventoryStorageAttachment.Item(-1, keptStack));

						stack.shrink(quota);
						entity.setItem(stack);
						quotas.put(key, 0);
					}
				}
			}
			return saved;
		}

		while (iterator.hasNext())
		{
			ItemEntity entity = iterator.next();
			ItemStack stack = entity.getItem();

			boolean keep = false;
			switch (mode)
			{
				case TAG -> keep = stack.is(FairerDeath.KEEP_AT_DEATH);
				case RANDOM -> keep = random.nextFloat() < Config.RANDOM_CHANCE.get();
				case CATEGORIES -> keep = categories.contains(Categories.KEEP_INVENTORY);
				case FULL -> keep = true;
			}

			if (keep)
			{
				saved.add(new InventoryStorageAttachment.Item(-1, stack.copy()));
				iterator.remove();
			}
		}
		return saved;
	}

	public static int getKeptExperience(ServerPlayer player, Modes mode, Set<Categories> categories)
	{
		return switch (mode)
		{
			case CATEGORIES -> categories.contains(Categories.KEEP_EXPERIENCE) ? player.totalExperience : 0;
			case RANDOM -> new Random().nextFloat() < Config.RANDOM_CHANCE.get() ? player.totalExperience : 0;
			case OLD -> {
				InventoryStorageAttachment tracker = player.getData(AttachmentTypeInit.STORAGE);
				yield tracker.getKeepXpQuota(player.level().getGameTime(), Config.AGE_TICKS.get());
			}
			case FULL -> player.totalExperience;
			case TAG -> 0;
		};
	}

	private static boolean keepByCategory(int slotId, Set<Categories> categories)
	{
		if (slotId >= 0 && slotId <= 8)
			return categories.contains(Categories.KEEP_HOTBAR);
		if (slotId >= 9 && slotId <= 35)
			return categories.contains(Categories.KEEP_INVENTORY);
		if (slotId >= 36 && slotId <= 39)
			return categories.contains(Categories.KEEP_ARMOR);
		if (slotId == 40)
			return categories.contains(Categories.KEEP_HOTBAR);
		return false;
	}
}
