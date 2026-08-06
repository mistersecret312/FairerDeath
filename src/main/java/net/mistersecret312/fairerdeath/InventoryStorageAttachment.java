package net.mistersecret312.fairerdeath;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.*;

public class InventoryStorageAttachment implements INBTSerializable<CompoundTag>
{
	public List<Item> savedItems = new ArrayList<>();
	public List<ItemStack> droppedItems = new ArrayList<>();
	public int keptExperience = 0;

	public final Map<ItemKey, LinkedList<ItemBatch>> tracker = new HashMap<>();
	public final Map<ItemKey, Integer> lastKnownCounts = new HashMap<>();
	public final LinkedList<XpBatch> xpTracker = new LinkedList<>();
	public int lastKnownXp = 0;

	public void updateTracker(ServerPlayer player)
	{
		long currentTime = player.level().getGameTime();
		Map<ItemKey, Integer> currentCounts = new HashMap<>();

		Inventory inv = player.getInventory();
		for (int i = 0; i < inv.getContainerSize(); i++)
		{
			ItemStack stack = inv.getItem(i);
			if (!stack.isEmpty())
			{
				ItemKey key = ItemKey.from(stack);
				currentCounts.put(key, currentCounts.getOrDefault(key, 0) + stack.getCount());
			}
		}

		for (Map.Entry<ItemKey, Integer> entry : currentCounts.entrySet())
		{
			ItemKey key = entry.getKey();
			int currentCount = entry.getValue();
			int lastCount = lastKnownCounts.getOrDefault(key, 0);

			LinkedList<ItemBatch> batches =
					tracker.computeIfAbsent(key, k -> new LinkedList<>());

			if (currentCount > lastCount)
				batches.addLast(new ItemBatch(currentTime, currentCount - lastCount));
			else if (currentCount < lastCount)
			{
				int amountLost = lastCount - currentCount;
				while (amountLost > 0 && !batches.isEmpty())
				{
					ItemBatch oldest = batches.getFirst();
					if (oldest.count <= amountLost)
					{
						amountLost -= oldest.count;
						batches.removeFirst();
					}
					else
					{
						oldest.count -= amountLost;
						amountLost = 0;
					}
				}
			}
		}

		int currentXp = player.totalExperience;
		if (currentXp > lastKnownXp)
			xpTracker.addLast(new XpBatch(currentTime, currentXp - lastKnownXp));
		else if (currentXp < lastKnownXp)
		{
			int amountLost = lastKnownXp - currentXp;
			while (amountLost > 0 && !xpTracker.isEmpty())
			{
				XpBatch oldest = xpTracker.getFirst();
				if (oldest.count <= amountLost)
				{
					amountLost -= oldest.count;
					xpTracker.removeFirst();
				}
				else
				{
					oldest.count -= amountLost;
					amountLost = 0;
				}
			}
		}
		lastKnownXp = currentXp;

		tracker.keySet().removeIf(key -> !currentCounts.containsKey(key));
		lastKnownCounts.clear();
		lastKnownCounts.putAll(currentCounts);
	}

	public Map<ItemKey, Integer> getKeepQuotas(long currentTime, long ticksRequired)
	{
		Map<ItemKey, Integer> quotas = new HashMap<>();
		for (Map.Entry<ItemKey, LinkedList<ItemBatch>> entry : tracker.entrySet())
		{
			int oldEnoughCount = 0;
			for (ItemBatch batch : entry.getValue())
			{
				if (currentTime - batch.timestamp >= ticksRequired)
					oldEnoughCount += batch.count;
			}
			if (oldEnoughCount > 0)
				quotas.put(entry.getKey(), oldEnoughCount);
		}
		return quotas;
	}

	public int getKeepXpQuota(long currentTime, long ticksRequired) {
		int oldEnoughXp = 0;
		for (XpBatch batch : xpTracker) {
			if (currentTime - batch.timestamp >= ticksRequired) {
				oldEnoughXp += batch.count;
			}
		}
		return oldEnoughXp;
	}

	@Override
	public @UnknownNullability CompoundTag serializeNBT(HolderLookup.Provider provider)
	{
		CompoundTag tag = new CompoundTag();

		ListTag savedList = new ListTag();
		for(Item savedItem : savedItems)
		{
			Optional<Tag> stackTag = ItemStack.CODEC.encodeStart(NbtOps.INSTANCE, savedItem.stack).resultOrPartial();
			if(stackTag.isPresent())
			{
				CompoundTag itemTag = new CompoundTag();
				itemTag.putInt("slot", savedItem.slot);
				itemTag.put("item", stackTag.get());
				savedList.add(itemTag);
			}
		}
		tag.put("saved_items", savedList);

		ListTag trackerList = new ListTag();
		for (Map.Entry<ItemKey, LinkedList<ItemBatch>> entry : this.tracker.entrySet())
		{
			CompoundTag entryTag = new CompoundTag();
			ItemKey key = entry.getKey();

			ItemStack dummyStack = new ItemStack(key.item(), 1);
			dummyStack.applyComponents(key.components());

			Optional<Tag> encodedItem = ItemStack.CODEC.encodeStart(NbtOps.INSTANCE, dummyStack).resultOrPartial();
			if (encodedItem.isEmpty())
				continue;
			entryTag.put("item_key", encodedItem.get());

			ListTag batchList = new ListTag();
			for (ItemBatch batch : entry.getValue())
			{
				CompoundTag batchTag = new CompoundTag();
				batchTag.putLong("timestamp", batch.timestamp);
				batchTag.putInt("count", batch.count);
				batchList.add(batchTag);
			}

			entryTag.put("batches", batchList);
			trackerList.add(entryTag);
		}
		tag.put("tracker", trackerList);
		tag.putInt("kept_xp", this.keptExperience);

		ListTag xpList = new ListTag();
		for (XpBatch batch : xpTracker)
		{
			CompoundTag batchTag = new CompoundTag();
			batchTag.putLong("timestamp", batch.timestamp);
			batchTag.putInt("count", batch.count);
			xpList.add(batchTag);
		}
		tag.put("xp_tracker", xpList);

		return tag;
	}

	@Override
	public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag)
	{
		this.savedItems.clear();
		this.tracker.clear();
		this.lastKnownCounts.clear();
		this.xpTracker.clear();
		this.droppedItems.clear();
		this.lastKnownXp = 0;

		ListTag listTag = tag.getList("saved_items", ListTag.TAG_COMPOUND);
		for(int i = 0; i < listTag.size(); i++)
		{
			CompoundTag itemTag = listTag.getCompound(i);
			int slot = itemTag.getInt("slot");
			Optional<ItemStack> optionalStack = ItemStack.CODEC.parse(NbtOps.INSTANCE, itemTag.get("item")).resultOrPartial();
			ItemStack stack = ItemStack.EMPTY;
			if(optionalStack.isPresent())
				stack = optionalStack.get();

			if(!stack.isEmpty())
				this.savedItems.add(new Item(slot, stack));
		}

		ListTag trackerList = tag.getList("tracker", Tag.TAG_COMPOUND);
		for (int i = 0; i < trackerList.size(); i++)
		{
			CompoundTag entryTag = trackerList.getCompound(i);

			Optional<ItemStack> decodedStack = ItemStack.CODEC.parse(NbtOps.INSTANCE, entryTag.get("item_key")).resultOrPartial();
			if (decodedStack.isEmpty() || decodedStack.get().isEmpty())
				continue;

			ItemKey key = ItemKey.from(decodedStack.get());
			LinkedList<ItemBatch> batches = new LinkedList<>();
			int totalCountForThisItem = 0;

			ListTag batchList = entryTag.getList("batches", Tag.TAG_COMPOUND);
			for (int j = 0; j < batchList.size(); j++)
			{
				CompoundTag batchTag = batchList.getCompound(j);
				long timestamp = batchTag.getLong("timestamp");
				int count = batchTag.getInt("count");

				batches.add(new ItemBatch(timestamp, count));
				totalCountForThisItem += count;
			}

			if (!batches.isEmpty())
			{
				this.tracker.put(key, batches);
				this.lastKnownCounts.put(key, totalCountForThisItem);
			}
		}

		this.keptExperience = tag.getInt("kept_xp");
		ListTag xpList = tag.getList("xp_tracker", Tag.TAG_COMPOUND);
		for (int i = 0; i < xpList.size(); i++)
		{
			CompoundTag batchTag = xpList.getCompound(i);
			int count = batchTag.getInt("count");
			this.xpTracker.add(new XpBatch(batchTag.getLong("timestamp"), count));
			this.lastKnownXp += count;
		}
	}

	public record Item(int slot, ItemStack stack) {}
	
	public record ItemKey(net.minecraft.world.item.Item item, DataComponentPatch components)
	{
		public static ItemKey from(ItemStack stack)
		{
			ItemStack normalized = stack.copy();

			if (normalized.isDamageableItem())
				normalized.setDamageValue(0);

			return new ItemKey(normalized.getItem(), normalized.getComponentsPatch());
		}
	}

	public static class ItemBatch
	{
		public final long timestamp;
		public int count;
		public ItemBatch(long timestamp, int count) { this.timestamp = timestamp; this.count = count; }
	}

	public static class XpBatch
	{
		public final long timestamp;
		public int count;
		public XpBatch(long timestamp, int count) { this.timestamp = timestamp; this.count = count; }
	}
}
