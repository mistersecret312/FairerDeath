package net.mistersecret312.fairerdeath;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

import static net.neoforged.fml.loading.FMLEnvironment.dist;

@Mod(FairerDeath.MODID)
public class FairerDeath
{
    public static final String MODID = "fairer_death";

    public FairerDeath(IEventBus modEventBus, ModContainer modContainer)
    {
        modEventBus.addListener(this::commonSetup);
        NeoForge.EVENT_BUS.register(this);

        modContainer.registerConfig(ModConfig.Type.SERVER, Config.SERVER_CONFIG);
        if(dist.isClient())
            modContainer.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {

    }


    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event)
    {
        for(Categories category : Config.getEnabledCategories())
        {
            System.out.println("loaded category - " + category);
        }
    }
}
