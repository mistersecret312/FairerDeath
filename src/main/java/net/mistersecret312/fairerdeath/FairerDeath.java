package net.mistersecret312.fairerdeath;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;

import static net.neoforged.fml.loading.FMLEnvironment.dist;

@Mod(FairerDeath.MODID)
public class FairerDeath
{
    public static final String MODID = "fairer_death";
    public static final TagKey<Item> KEEP_AT_DEATH = TagKey.create(Registries.ITEM,
            ResourceLocation.fromNamespaceAndPath(MODID, "keep_at_death"));

    public FairerDeath(IEventBus modEventBus, ModContainer modContainer)
    {
        NeoForge.EVENT_BUS.register(this);

        AttachmentTypeInit.register(modEventBus);
        modContainer.registerConfig(ModConfig.Type.SERVER, Config.SERVER_CONFIG);
        if(dist.isClient())
            modContainer.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }
}
