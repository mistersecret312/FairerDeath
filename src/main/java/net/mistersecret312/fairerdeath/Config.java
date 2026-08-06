package net.mistersecret312.fairerdeath;

import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Config
{
    private static final ModConfigSpec.Builder SERVER_BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec SERVER_CONFIG;

    public static final ModConfigSpec.ConfigValue<List<? extends String>> CATEGORIES;
    public static final ModConfigSpec.ConfigValue<Modes> MODE;

    public static final ModConfigSpec.DoubleValue RANDOM_CHANCE;

    static
    {
        MODE = SERVER_BUILDER
                       .comment("Current Mode")
                       .defineEnum("mode", Modes.OLD);
        RANDOM_CHANCE = SERVER_BUILDER
                                .comment("If Mode is Random, will use this chance for the chance to keep an item")
                                .defineInRange("random_chance", 0.5d, 0d, 1d);

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
