package me.gimme.gimmeores.generation.populators;

import me.gimme.gimmeores.chunk.PopulatedChunksData;
import me.gimme.gimmeores.generation.Populator;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Random;
import java.util.Set;

/**
 * Generates single blocks.
 */
public class SinglePopulator extends Populator {
    /**
     * @param plugin              the plugin
     * @param populatedChunksData the data of populated chunks
     * @param type                the material of the blocks to generate, or null for all (only for removing)
     * @param types               the materials of the blocks to generate, or null
     * @param size                the size of the generation
     * @param triesPerChunk       number of generation attempts per chunk
     * @param minHeight           the min height to generate at
     * @param maxHeight           the max height to generate at
     * @param replaceWith         type of block to replace previous instances of the specified material with, or null for not removing
     * @param replaceRestWith     type of block to replace all other blocks with, or null for not removing
     * @param canReplace          the type of blocks that can be replaced in the generation, or null for all
     * @param worlds              the worlds to generate in
     * @param biomes              the biomes to generate in, or null for all biomes
     */
    public SinglePopulator(@NotNull Plugin plugin, @NotNull PopulatedChunksData populatedChunksData, @Nullable Material type,
                     @Nullable Collection<Material> types, int size, double triesPerChunk, int minHeight, int maxHeight,
                     @Nullable Material replaceWith, @Nullable Material replaceRestWith, @Nullable Set<Material> canReplace,
                     @NotNull Set<String> worlds, @Nullable Set<Biome> biomes) {
        super(plugin, populatedChunksData, type, types, size, triesPerChunk, minHeight, maxHeight, replaceWith,
                replaceRestWith, canReplace, worlds, biomes);
    }

    @Override
    protected void generate(@NotNull World world, @NotNull Chunk sourceChunk, @NotNull Random random, int size, int x, int y, int z) {
        setBlock(world, sourceChunk, random, x, y, z);
    }
}
