package net.mistersecret312.fairerdeath;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Config
{
	private static final ForgeConfigSpec.Builder SERVER_BUILDER = new ForgeConfigSpec.Builder();
	public static final ForgeConfigSpec SERVER_CONFIG;

	public static final ForgeConfigSpec.ConfigValue<List<? extends String>> CATEGORIES;
	public static final ForgeConfigSpec.ConfigValue<Modes> MODE;

	public static final ForgeConfigSpec.IntValue AGE_TICKS;
	public static final ForgeConfigSpec.DoubleValue RANDOM_CHANCE;

	static
	{
		MODE = SERVER_BUILDER
					   .comment("Current Mode")
					   .defineEnum("mode", Modes.AGE);
		RANDOM_CHANCE = SERVER_BUILDER
								.comment("If Mode is Random, will use this chance for the chance to keep an item")
								.defineInRange("random_chance", 0.5d, 0d, 1d);
		AGE_TICKS = SERVER_BUILDER
							.comment("If Mode is Old, will use this as amount of ticks you must have an item for, for it to be kept at death")
							.defineInRange("age_ticks", 1200, 0, Integer.MAX_VALUE);

		CATEGORIES = SERVER_BUILDER
							 .comment("A list of enabled categories")
							 .defineListAllowEmpty(
									 List.of("categories"),
									 () -> List.of(Categories.KEEP_INVENTORY.name(), Categories.KEEP_ARMOR.name(),
											 Categories.KEEP_EXPERIENCE.name(), Categories.KEEP_HOTBAR.name()),
									 obj -> obj instanceof String str && Categories.isKnown(str)
							 );

		SERVER_CONFIG = SERVER_BUILDER.build();
	}

	public static Set<Categories> getEnabledCategories() {
		return CATEGORIES.get().stream()
						 .map(Categories::valueOf)
						 .collect(Collectors.toUnmodifiableSet());
	}
}

