package me.gimme.gimmeores.chunk;

import me.gimme.gimmeores.generation.Populator;
import me.gimme.gimmeores.generation.PopulatorFactory;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Used for creating Populator objects from the config settings.
 */
public class PopulatorSettings {
    private static final String CONFIG_MATERIAL = "material";
    private static final String CONFIG_TYPE = "type";
    private static final String CONFIG_SIZE = "size";
    private static final String CONFIG_TRIES = "tries";
    private static final String CONFIG_MIN_HEIGHT = "min-height";
    private static final String CONFIG_MAX_HEIGHT = "max-height";
    private static final String CONFIG_REPLACE_WITH = "replace-with";
    private static final String CONFIG_CAN_REPLACE = "can-replace";
    private static final String CONFIG_WORLDS = "worlds";
    private static final String CONFIG_BIOMES = "biomes";

    private Map<String, ?> populatorSettings;
    @Nullable
    private ConfigurationSection defaultSettings;
    private Plugin plugin;
    private PopulatedChunksData populatedChunksData;

    public PopulatorSettings(@NotNull Map<String, ?> populatorSettings, @Nullable ConfigurationSection defaultSettings,
                              @NotNull Plugin plugin, @NotNull PopulatedChunksData populatedChunksData) {
        this.populatorSettings = populatorSettings;
        this.defaultSettings = defaultSettings;
        this.plugin = plugin;
        this.populatedChunksData = populatedChunksData;
    }

    /**
     * @return a new populator from the settings
     */
    @Nullable
    public Populator createPopulator() {
        Material material = Material.matchMaterial(get(CONFIG_MATERIAL, ""));
        if (material == null) return null;
        String type = get(CONFIG_TYPE, "VEIN");
        int size = get(CONFIG_SIZE, 1);
        double tries = getDouble(CONFIG_TRIES, 0d);
        int minHeight = get(CONFIG_MIN_HEIGHT, 0);
        int maxHeight = get(CONFIG_MAX_HEIGHT, 255);
        @Nullable String replaceWithString = getNullable(CONFIG_REPLACE_WITH, null);
        @Nullable List<String> canReplaceStrings = getNullable(CONFIG_CAN_REPLACE, null);
        Set<String> worlds = new HashSet<>(get(CONFIG_WORLDS, Collections.singletonList("world")));
        @Nullable List<String> biomeStrings = getNullable(CONFIG_BIOMES, null);

        PopulatorFactory.PopulatorType populatorType = PopulatorFactory.PopulatorType.VEIN;
        for (PopulatorFactory.PopulatorType pt : PopulatorFactory.PopulatorType.values()) {
            if (pt.toString().equals(type)) populatorType = pt;
        }

        Material replaceWith = replaceWithString == null ? null : Material.matchMaterial(replaceWithString);

        Set<Material> canReplace;
        if (canReplaceStrings == null) {
            canReplace = null;
        } else {
            canReplace = new HashSet<>();
            for (String canReplaceString : canReplaceStrings) {
                Material mat = Material.matchMaterial(canReplaceString);
                if (mat != null) canReplace.add(mat);
            }
        }

        Set<Biome> biomes;
        if (biomeStrings == null) {
            biomes = null;
        } else {
            biomes = new HashSet<>();
            for (String biomeString : biomeStrings) {
                for (Biome bio : Biome.values()) {
                    if (bio.toString().equals(biomeString)) {
                        biomes.add(bio);
                        break;
                    }
                }
            }
        }

        return PopulatorFactory.createPopulator(plugin, populatedChunksData,
                populatorType, material, size, tries, minHeight, maxHeight, replaceWith,
                canReplace, worlds, biomes
        );
    }

    /**
     * @return the double value from the settings at the specified path, or the default value from the config,
     * or the specified default value
     * @param path the path to get the value from in the populator settings
     * @param def the last resort default value
     */
    private double getDouble(@NotNull String path, double def) {
        Number value = (Number) populatorSettings.get(path);
        if (value != null) return value.doubleValue();
        if (defaultSettings != null && defaultSettings.contains(path)) return defaultSettings.getDouble(path);
        return def;
    }

    /**
     * @return the value from the settings at the specified path, or the default value from the config,
     * or the specified default value
     * @param path the path to get the value from in the populator settings
     * @param def the last resort default value
     * @param <T> the type of the setting value
     */
    @SuppressWarnings("unchecked")
    @NotNull
    private <T> T get(@NotNull String path, @NotNull T def) {
        T value = (T) populatorSettings.get(path);
        if (value != null) return value;
        if (defaultSettings != null && defaultSettings.contains(path))
            return (T) Objects.requireNonNull(defaultSettings.get(path));
        return def;
    }

    /**
     * @return the nullable value from the settings at the specified path, or the default value from the config,
     * or the specified default value
     * @param path the path to get the value from in the populator settings
     * @param def the last resort default value
     * @param <T> the type of the setting value
     */
    @SuppressWarnings("unchecked")
    @Nullable
    private <T> T getNullable(@NotNull String path, @Nullable T def) {
        if (populatorSettings.containsKey(path)) return (T) populatorSettings.get(path);
        if (defaultSettings != null && defaultSettings.contains(path)) return (T) defaultSettings.get(path);
        return def;
    }
}
