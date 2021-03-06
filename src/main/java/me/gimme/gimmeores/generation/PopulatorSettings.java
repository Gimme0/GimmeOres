package me.gimme.gimmeores.generation;

import me.gimme.gimmeores.chunk.PopulatedChunksData;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

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
    private static final String CONFIG_REPLACE_REST_WITH = "replace-rest-with";
    private static final String CONFIG_CAN_REPLACE = "can-replace";
    private static final String CONFIG_WORLDS = "worlds";
    private static final String CONFIG_BIOMES = "biomes";

    private static final List<String> NULL_EQUIVALENT_STRINGS = Arrays.asList("all", "none");

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
        List<String> materialStrings;
        if (isAssignableFrom(List.class, CONFIG_MATERIAL)) {
            materialStrings = getNullable(CONFIG_MATERIAL, null);
        } else {
            String materialString = getNullable(CONFIG_MATERIAL, null);
            materialStrings = materialString != null
                    ? new ArrayList<>(Collections.singletonList(materialString))
                    : null;
        }
        List<Material> materials = materialStrings != null
                ? materialStrings.stream()
                .map(Material::matchMaterial)
                .filter(Objects::nonNull)
                .collect(Collectors.toList())
                : null;
        String type = get(CONFIG_TYPE, "VEIN");
        int size = get(CONFIG_SIZE, 1);
        double tries = getDouble(CONFIG_TRIES, 0d);
        int minHeight = get(CONFIG_MIN_HEIGHT, 0);
        int maxHeight = get(CONFIG_MAX_HEIGHT, 255);
        @Nullable String replaceWithString = getNullable(CONFIG_REPLACE_WITH, null);
        @Nullable String replaceRestWithString = getNullable(CONFIG_REPLACE_REST_WITH, null);
        @Nullable List<String> canReplaceStrings = getNullable(CONFIG_CAN_REPLACE, null);
        Set<String> worlds = new HashSet<>(get(CONFIG_WORLDS, Collections.singletonList("world")));
        @Nullable List<String> biomeStrings = getNullable(CONFIG_BIOMES, null);

        PopulatorFactory.PopulatorType populatorType = PopulatorFactory.PopulatorType.VEIN;
        for (PopulatorFactory.PopulatorType pt : PopulatorFactory.PopulatorType.values()) {
            if (pt.toString().equals(type)) populatorType = pt;
        }

        Material replaceWith = replaceWithString == null ? null : Material.matchMaterial(replaceWithString);
        Material replaceRestWith = replaceRestWithString == null ? null : Material.matchMaterial(replaceRestWithString);

        Set<Material> canReplace = canReplaceStrings != null
                ? canReplaceStrings.stream()
                .map(Material::matchMaterial)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet())
                : null;

        Set<Biome> biomes;
        if (biomeStrings == null) {
            biomes = null;
        } else {
            biomes = new HashSet<>();
            for (String biomeString : biomeStrings) {
                for (Biome bio : Biome.values()) {
                    if (bio.toString().equalsIgnoreCase(biomeString)) {
                        biomes.add(bio);
                        break;
                    }
                }
            }
        }

        return PopulatorFactory.createPopulator(plugin, populatedChunksData,
                populatorType, materials, size, tries, minHeight, maxHeight, replaceWith, replaceRestWith,
                canReplace, worlds, biomes
        );
    }

    /**
     * @param path the path to get the value from in the populator settings
     * @param def  the last resort default value
     * @return the double value from the settings at the specified path, or the default value from the config,
     * or the specified default value
     */
    private double getDouble(@NotNull String path, double def) {
        Number value = (Number) populatorSettings.get(path);
        if (value != null) return value.doubleValue();
        if (defaultSettings != null && defaultSettings.contains(path)) return defaultSettings.getDouble(path);
        return def;
    }

    /**
     * @param path the path to get the value from in the populator settings
     * @param def  the last resort default value
     * @param <T>  the type of the setting value
     * @return the value from the settings at the specified path, or the default value from the config,
     * or the specified default value
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
     * @param path the path to get the value from in the populator settings
     * @param def  the last resort default value
     * @param <T>  the type of the setting value
     * @return the nullable value from the settings at the specified path, or the default value from the config,
     * or the specified default value
     */
    @SuppressWarnings("unchecked")
    @Nullable
    private <T> T getNullable(@NotNull String path, @Nullable T def) {
        Object value;
        if (populatorSettings.containsKey(path)) {
            value = populatorSettings.get(path);
        } else if (defaultSettings != null && defaultSettings.contains(path)) {
            value = defaultSettings.get(path);
        } else {
            return def;
        }

        if (value == null) return null;

        if (String.class.isAssignableFrom(value.getClass()) && NULL_EQUIVALENT_STRINGS.stream().anyMatch(s -> s.equalsIgnoreCase((String) value))) {
            return null;
        }

        return (T) value;
    }

    /**
     * Determines if the class or interface represented by the specified {@code Class} parameter is either the same as,
     * or is a superclass or superinterface of, the value from the settings at the specified path, or the default value
     * from the config.
     *
     * @param cls  the {@code Class} object to be checked against
     * @param path the path to get the value from in the populator settings
     * @return if objects of the type of the value at the specified settings path can be assigned to objects of the
     * specified class type
     */
    private boolean isAssignableFrom(@NotNull Class<?> cls, @NotNull String path) {
        Object value = populatorSettings.get(path);
        if (value == null && defaultSettings != null && defaultSettings.contains(path))
            value = defaultSettings.get(path);
        if (value == null) return true;
        return cls.isAssignableFrom(value.getClass());
    }
}
