package net.mistersecret312.fairerdeath;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingExperienceDropEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.items.ItemHandlerHelper;

import java.util.List;
import java.util.Set;

@EventBusSubscriber(modid = FairerDeath.MODID, bus = EventBusSubscriber.Bus.GAME)
public class CommonEvents
{
	@SubscribeEvent
	public static void onPlayerTick(PlayerTickEvent.Post event)
	{
		if (event.getEntity().level().isClientSide() || event.getEntity().tickCount % 20 != 0)
			return;

		if (Config.KEEPING_MODE.get() == Modes.AGE && event.getEntity() instanceof ServerPlayer player)
		{
			InventoryStorageAttachment storage = player.getData(AttachmentTypeInit.STORAGE);
			storage.updateTracker(player);
		}
	}

	@SubscribeEvent
	public static void onPlayerDeath(LivingDeathEvent event)
	{
		if (!(event.getEntity() instanceof ServerPlayer player))
			return;

		InventoryStorageAttachment storage = player.getData(AttachmentTypeInit.STORAGE);
		Modes mode = Config.KEEPING_MODE.get();
		Set<Categories> categories = Config.getEnabledCategories();

		List<InventoryStorageAttachment.Item> keptItems = InventoryUtil.extractItems(player, mode, categories, storage.droppedItems);
		storage.savedItems.addAll(keptItems);

		storage.keptExperience = InventoryUtil.getKeptExperience(player, mode, categories);
	}

	@SubscribeEvent(priority = EventPriority.LOW)
	public static void onPlayerDrop(LivingDropsEvent event)
	{
		if (!(event.getEntity() instanceof ServerPlayer player))
			return;

		InventoryStorageAttachment storage = player.getData(AttachmentTypeInit.STORAGE);
		Modes mode = Config.KEEPING_MODE.get();
		Set<Categories> categories = Config.getEnabledCategories();

		List<InventoryStorageAttachment.Item> keptDrops = InventoryUtil.extractItems(player, event.getDrops(), mode, categories, storage.droppedItems);
		storage.savedItems.addAll(keptDrops);
	}

	@SubscribeEvent
	public static void onExperienceDrop(LivingExperienceDropEvent event)
	{
		if(!(event.getEntity() instanceof ServerPlayer player))
			return;

		InventoryStorageAttachment storage = player.getData(AttachmentTypeInit.STORAGE);
		if(storage.keptExperience > 0)
		{
			int vanillaDrop = event.getDroppedExperience();
			event.setDroppedExperience(Math.max(0, vanillaDrop - storage.keptExperience));
		}
	}

	@SubscribeEvent
	public static void onPlayerClone(PlayerEvent.Clone event)
	{
		if (!event.isWasDeath() || !(event.getEntity() instanceof ServerPlayer serverPlayer))
			return;

		ServerPlayer oldPlayer = (ServerPlayer) event.getOriginal();
		InventoryStorageAttachment storage = oldPlayer.getData(AttachmentTypeInit.STORAGE);

		Inventory inv = serverPlayer.getInventory();

		for (InventoryStorageAttachment.Item saved : storage.savedItems)
		{
			if (saved.slot() >= 0 && saved.slot() < inv.getContainerSize())
				inv.setItem(saved.slot(), saved.stack().copy());
			else if(!inv.add(saved.slot(), saved.stack().copy()))
					serverPlayer.drop(saved.stack().copy(), true, false);
		}

		if (storage.keptExperience > 0)
			serverPlayer.giveExperiencePoints(storage.keptExperience);

		serverPlayer.getData(AttachmentTypeInit.STORAGE).tracker.putAll(storage.tracker);
	}
}
