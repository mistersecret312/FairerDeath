package net.mistersecret312.fairerdeath;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Config
{
	private static final ForgeConfigSpec.Builder SERVER_BUILDER = new ForgeConfigSpec.Builder();
	public static final ForgeConfigSpec SERVER_CONFIG;

	public static final ForgeConfigSpec.ConfigValue<List<? extends String>> CATEGORIES_TO_KEEP;
	public static final ForgeConfigSpec.ConfigValue<Modes> KEEPING_MODE;

	public static final ForgeConfigSpec.IntValue AGE_TO_KEEP;
	public static final ForgeConfigSpec.DoubleValue CHANCE_TO_KEEP;

	static
	{
		KEEPING_MODE = SERVER_BUILDER
				.comment("Select inventory keeping mode here. It defines the manner in which items and experience are preserved.")
				.comment("Allowed values: FULL, AGE, CHANCE, CATEGORIES, TAG")
				.comment("FULL - Vanilla keep inventory, full experience and item keeping at all times.")
				.comment("AGE - Only items that have been in your inventory for X amount of time are kept. X is defined lower at age_ticks.")
				.comment("CHANCE - For every item in your inventory, roll a chance X for it to be kept. X is defined lower at random_chance.")
				.comment("CATEGORIES - Only items in selected slot categories will be kept. The list is defined lower at categories_to_keep.")
				.comment("TAG - Only item IDs defined in the fairer_death:keep_at_death item tag will be kept.")
				.comment("Default value: AGE")
				.defineEnum("keeping_mode", Modes.AGE);
		CHANCE_TO_KEEP = SERVER_BUILDER
				.comment("If mode is CHANCE, will use this chance to roll to keep an item or half of experience.")
				.comment("Default value: 0.5")
				.defineInRange("chance_to_keep", 0.5d, 0d, 1d);
		AGE_TO_KEEP = SERVER_BUILDER
				.comment("If mode is AGE, the item will be kept on death if it was for this long in the inventory, same logic for experience.")
				.comment("Default value: 1200 (60 seconds)")
				.defineInRange("age_to_keep", 1200, 0, Integer.MAX_VALUE);
		CATEGORIES_TO_KEEP = SERVER_BUILDER
				.comment("If mode is CATEGORIES, the items in the slot categories selected here will be kept on death.")
				.comment("Allowed values: KEEP_INVENTORY, KEEP_HOTBAR, KEEP_ARMOR, KEEP_EXPERIENCE")
				.comment("Default values: KEEP_HOTBAR, KEEP_ARMOR")
				.defineListAllowEmpty(
						List.of("categories_to_keep"),
						() -> List.of(Categories.KEEP_HOTBAR.name(), Categories.KEEP_ARMOR.name()),
						obj -> obj instanceof String str && Categories.isKnown(str)
				);

		SERVER_CONFIG = SERVER_BUILDER.build();
	}

	public static Set<Categories> getEnabledCategories() {
		return CATEGORIES_TO_KEEP.get().stream()
				.map(Categories::valueOf)
				.collect(Collectors.toUnmodifiableSet());
	}
}

