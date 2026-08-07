package net.mistersecret312.fairerdeath;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Mod.EventBusSubscriber(modid = FairerDeath.MODID)
public class CommonEvents
{
	@SubscribeEvent
	public static void onPlayerTick(TickEvent.PlayerTickEvent event)
	{
		if (event.player.level().isClientSide() || event.player.tickCount % 20 != 0)
			return;

		if (Config.MODE.get() == Modes.OLD && event.player instanceof ServerPlayer player)
		{
			Optional<InventoryStorageCapability> trackerOptional =
					player.getCapability(CapabilityInit.STORAGE).resolve();
			if(trackerOptional.isEmpty())
				return;

			InventoryStorageCapability tracker = trackerOptional.get();
			tracker.updateTracker(player);
		}
	}

	@SubscribeEvent
	public static void onPlayerDeath(LivingDeathEvent event)
	{
		if (!(event.getEntity() instanceof ServerPlayer player))
			return;

		Optional<InventoryStorageCapability> trackerOptional =
				player.getCapability(CapabilityInit.STORAGE).resolve();
		if(trackerOptional.isEmpty())
			return;

		InventoryStorageCapability tracker = trackerOptional.get();
		Modes mode = Config.MODE.get();
		Set<Categories> categories = Config.getEnabledCategories();

		List<InventoryStorageCapability.Item> keptItems = InventoryUtil.extractItems(player, mode, categories, tracker.droppedItems);
		tracker.savedItems.addAll(keptItems);

		tracker.keptExperience = InventoryUtil.getKeptExperience(player, mode, categories);
	}

	@SubscribeEvent(priority = EventPriority.LOW)
	public static void onPlayerDrop(LivingDropsEvent event)
	{
		if (!(event.getEntity() instanceof ServerPlayer player))
			return;

		Optional<InventoryStorageCapability> trackerOptional =
				player.getCapability(CapabilityInit.STORAGE).resolve();
		if(trackerOptional.isEmpty())
			return;

		InventoryStorageCapability tracker = trackerOptional.get();
		Modes mode = Config.MODE.get();
		Set<Categories> categories = Config.getEnabledCategories();

		List<InventoryStorageCapability.Item> keptDrops = InventoryUtil.extractItems(player, event.getDrops(), mode, categories, tracker.droppedItems);
		tracker.savedItems.addAll(keptDrops);
	}

	@SubscribeEvent
	public static void onExperienceDrop(LivingExperienceDropEvent event)
	{
		if(!(event.getEntity() instanceof ServerPlayer player))
			return;

		Optional<InventoryStorageCapability> trackerOptional =
				player.getCapability(CapabilityInit.STORAGE).resolve();
		if(trackerOptional.isEmpty())
			return;

		InventoryStorageCapability tracker = trackerOptional.get();
		if(tracker.keptExperience > 0)
		{
			int vanillaDrop = event.getDroppedExperience();
			event.setDroppedExperience(Math.max(0, vanillaDrop - tracker.keptExperience));
		}
	}

	@SubscribeEvent
	public static void onPlayerClone(PlayerEvent.Clone event)
	{
		if (!event.isWasDeath() || !(event.getEntity() instanceof ServerPlayer serverPlayer))
		{
			return;
		}


		ServerPlayer oldPlayer = (ServerPlayer) event.getOriginal();
		oldPlayer.reviveCaps();
		Optional<InventoryStorageCapability> trackerOptional =
				oldPlayer.getCapability(CapabilityInit.STORAGE).resolve();
		if(trackerOptional.isEmpty())
		{
			return;
		}


		InventoryStorageCapability tracker = trackerOptional.get();

		Inventory inv = serverPlayer.getInventory();

		for (InventoryStorageCapability.Item saved : tracker.savedItems)
		{
			if (saved.slot() >= 0 && saved.slot() < inv.getContainerSize())
				inv.setItem(saved.slot(), saved.stack().copy());
			else if(!inv.add(saved.slot(), saved.stack().copy()))
					serverPlayer.drop(saved.stack().copy(), true, false);
		}

		if (tracker.keptExperience > 0)
			serverPlayer.giveExperiencePoints(tracker.keptExperience);
	}
}
