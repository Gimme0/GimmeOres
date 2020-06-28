package me.gimme.gimmeores.generation;

import me.gimme.gimmeores.chunk.PopulatedChunksData;
import me.gimme.gimmeores.generation.populators.SinglePopulator;
import me.gimme.gimmeores.generation.populators.VeinPopulator;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public abstract class PopulatorFactory {
    public enum PopulatorType {
        VEIN,
        SINGLE
    }

    /**
     * Creates a populator based on the specified type.
     *
     * @param plugin              the plugin
     * @param populatedChunksData the data of populated chunks
     * @param populatorType       the populator type to create
     * @param material            the material of the blocks to generate, or null for all (only for removing)
     * @param size                the size of the generation
     * @param triesPerChunk       number of generation attempts per chunk
     * @param minHeight           the min height to generate at
     * @param maxHeight           the max height to generate at
     * @param replaceWith         type of block to replace previous instances of the specified material with, or null for not removing
     * @param canReplace          the type of blocks that can be replaced in the generation, or null for all
     * @param worlds              the worlds to generate in
     * @param biomes              the biomes to generate in, or null for all biomes
     * @return a new populator based on the specified type
     */
    @NotNull
    public static Populator createPopulator(
            @NotNull Plugin plugin, @NotNull PopulatedChunksData populatedChunksData,
            @NotNull PopulatorType populatorType, @Nullable Material material,
            int size, double triesPerChunk, int minHeight, int maxHeight, @Nullable Material replaceWith,
            @Nullable Set<Material> canReplace, @NotNull Set<String> worlds, @Nullable Set<Biome> biomes) {
        switch (populatorType) {
            default:
            case VEIN:
                return new VeinPopulator(plugin, populatedChunksData, material, size, triesPerChunk, minHeight, maxHeight,
                        replaceWith, canReplace, worlds, biomes);
            case SINGLE:
                return new SinglePopulator(plugin, populatedChunksData, material, size, triesPerChunk, minHeight, maxHeight,
                        replaceWith, canReplace, worlds, biomes);
        }
    }
}
