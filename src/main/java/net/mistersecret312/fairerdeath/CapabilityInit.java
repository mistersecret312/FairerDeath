package net.mistersecret312.fairerdeath;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = FairerDeath.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CapabilityInit
{
    public static final Capability<InventoryStorageCapability> STORAGE =
            CapabilityManager.get(new CapabilityToken<>() {});

    @SubscribeEvent
    public static void register(RegisterCapabilitiesEvent event)
    {
        event.register(InventoryStorageCapability.class);
    }
}
