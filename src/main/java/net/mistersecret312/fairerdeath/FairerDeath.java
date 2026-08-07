package net.mistersecret312.fairerdeath;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;

@Mod(FairerDeath.MODID)
public class FairerDeath
{
	public static final String MODID = "fairer_death";
	public static final TagKey<Item> KEEP_AT_DEATH = TagKey.create(Registries.ITEM,
			ResourceLocation.fromNamespaceAndPath(MODID, "keep_at_death"));

	public FairerDeath()
	{
		ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, Config.SERVER_CONFIG);
	}
}
