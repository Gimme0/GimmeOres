package me.gimme.gimmeores.generation;

import me.gimme.gimmeores.chunk.PopulatedChunksData;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * A populator is responsible for generating a small area of blocks.
 * For example, generating ores in the ground.
 */
public abstract class Populator {
    private static final String METADATA_KEY = "populated";

    @NotNull
    private Plugin plugin;
    @NotNull
    private PopulatedChunksData populatedChunksData;

    @NotNull
    private Material type;
    private int size;
    private int minHeight;
    private int maxHeight;
    private double triesPerChunk;
    @Nullable
    private Material replaceWith;
    @Nullable
    private Set<Material> canReplace;
    @NotNull
    private Set<String> worlds;
    @Nullable
    private Set<Biome> biomes;

    /**
     * @param plugin              the plugin
     * @param populatedChunksData the data of populated chunks
     * @param type                the material of the blocks to generate
     * @param size                the size of the generation
     * @param triesPerChunk       number of generation attempts per chunk
     * @param minHeight           the min height to generate at
     * @param maxHeight           the max height to generate at
     * @param replaceWith         type of block to replace previous instances of the specified material with, or null for not removing
     * @param canReplace          the type of blocks that can be replaced in the generation, or null for all
     * @param worlds              the worlds to generate in
     * @param biomes              the biomes to generate in, or null for all biomes
     */
    public Populator(@NotNull Plugin plugin, @NotNull PopulatedChunksData populatedChunksData, @NotNull Material type,
                     int size, double triesPerChunk, int minHeight, int maxHeight,
                     @Nullable Material replaceWith, @Nullable Set<Material> canReplace,
                     @NotNull Set<String> worlds, @Nullable Set<Biome> biomes) {
        this.plugin = plugin;
        this.populatedChunksData = populatedChunksData;

        this.type = type;
        this.size = size;
        this.triesPerChunk = triesPerChunk;
        this.minHeight = minHeight;
        this.maxHeight = maxHeight;
        this.replaceWith = replaceWith;
        this.canReplace = canReplace;
        if (this.canReplace != null) this.canReplace.add(type);
        this.worlds = worlds;
        this.biomes = biomes;
    }

    /**
     * Populates blocks for the specified chunk.
     * <p>
     * Worlds and biomes that this populator does not cover are ignored. If this populator has a replaceWith block,
     * the covered area in the chunk is first unpopulated of blocks of this populator's type, replacing them with that block.
     *
     * @param source the chunk to generate for
     * @param random the random generator to use
     */
    public void populate(@NotNull Chunk source, @NotNull Random random) {
        World world = source.getWorld();
        if (!worlds.contains(world.getName())) return;

        int cx = source.getX();
        int cz = source.getZ();

        // Check the four center blocks of the chunk. At least one of them has to have the correct biome.
        if (biomes != null) {
            int correctBiomeCount = 0;
            for (int i = 0; i <= 1; i++) {
                for (int j = 0; j <= 1; j++) {
                    if (biomes.contains(world.getBiome(cx * 16 + 7 + i, minHeight + (maxHeight - minHeight) / 2, cz * 16 + 7 + j))) {
                        correctBiomeCount++;
                    }
                }
            }
            if (correctBiomeCount == 0) return;
        }

        if (replaceWith != null) unpopulate(source, replaceWith);
        if (size == 0 || triesPerChunk == 0) return;

        int offset = 8;

        int baseVeins = (int) Math.floor(triesPerChunk);
        double varVeins = triesPerChunk - baseVeins;
        int veins = baseVeins + (random.nextDouble() < varVeins ? 1 : 0);

        for (int i = 0; i < veins; i++) {
            int x = cx * 16 + random.nextInt(16) + offset;
            int z = cz * 16 + random.nextInt(16) + offset;
            int y = random.nextInt(maxHeight + 1 - minHeight) + minHeight;

            generate(world, source, random, size, x, y, z);
        }
    }

    /**
     * Generates some number of blocks with the type of this populator with an origin at the specified coordinates.
     *
     * @param world       the world to generate in
     * @param sourceChunk the chunk to generate for
     * @param random      the random generator to use
     * @param size        the size of the generation (vein)
     * @param x           the X-coordinate of the origin block
     * @param y           the Y-coordinate of the origin block
     * @param z           the Z-coordinate of the origin block
     */
    abstract protected void generate(@NotNull World world, @NotNull Chunk sourceChunk, @NotNull Random random,
                                     int size, int x, int y, int z);

    /**
     * Sets the block at the specified position to the type of this populator.
     * <p>
     * Only sets the block if xyz are within a 32*256*32 box (the four chunks made up of the source chunk and its
     * neighbors up to x + 1 and z + 1), and the current type at the position can be replaced.
     *
     * @param world       the world to set the block in
     * @param sourceChunk the source chunk that caused the population
     * @param x           the X-coordinate of the block
     * @param y           the Y-coordinate of the block
     * @param z           the Z-coordinate of the block
     */
    protected void setBlock(@NotNull World world, @NotNull Chunk sourceChunk, int x, int y, int z) {
        // Check y-bounds of chunk
        if (y < 0 || 255 < y) return;
        // Check if x and z are within the 2x2 chunks with the center offset by 8 from the source chunk's center
        int centerX = sourceChunk.getX() * 16 + 16;
        int centerZ = sourceChunk.getZ() * 16 + 16;
        if (x < centerX - 16 || centerX + 16 <= x) return;
        if (z < centerZ - 16 || centerZ + 16 <= z) return;

        Block block = getBlock(world, x, y, z);
        if (canReplace != null && !canReplace.contains(block.getType())) return;

        block.setType(type, false);

        // If the block is in a chunk that hasn't been populated yet, mark it so that it doesn't get removed by
        // unpopulation later.
        Set<Point> populatedChunks = populatedChunksData.getPopulatedChunks(world);
        if (!isInChunk(x, z, sourceChunk) && !populatedChunks.contains(new Point(x >> 4, z >> 4))) {
            block.setMetadata(METADATA_KEY, new FixedMetadataValue(plugin, populatedChunksData.getId(world)));
        }
    }

    /**
     * Removes previous instances of blocks with the type of this populator in the specified chunk.
     * Only removes blocks in areas covered by this populator.
     *
     * @param chunk       the chunk to unpopulate
     * @param replaceWith the type to replace the removed block with
     */
    private void unpopulate(@NotNull Chunk chunk, @NotNull Material replaceWith) {
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = getMinGenerationHeight(); y <= getMaxGenerationHeight(); y++) {
                    Block block = chunk.getBlock(x, y, z);
                    if (!block.getType().equals(type)) continue;

                    // If marked as having been populated by this plugin, don't remove it
                    List<MetadataValue> metadataValues = block.getMetadata(METADATA_KEY);
                    if (!metadataValues.isEmpty() && metadataValues.get(0).asString().equals(populatedChunksData.getId(chunk.getWorld()))) {
                        block.removeMetadata(METADATA_KEY, plugin);
                        continue;
                    }

                    block.setType(replaceWith, false);
                }
            }
        }
    }

    /**
     * @param world the world to get the block from
     * @param x     the X-coordinate of the block
     * @param y     the Y-coordinate of the block
     * @param z     the Z-coordinate of the block
     * @return the block at the specified coordinates from the specified world
     */
    @NotNull
    private Block getBlock(@NotNull World world, int x, int y, int z) {
        return world.getChunkAt(x >> 4, z >> 4).getBlock(x & 15, y, z & 15);
    }

    /**
     * @param x     the X-coordinate
     * @param z     the Z-coordinate
     * @param chunk the chunk to check if containing the coordinates
     * @return if the specified coordinates are within the specified chunk
     */
    private boolean isInChunk(int x, int z, @NotNull Chunk chunk) {
        return chunk.getX() == x >> 4 && chunk.getZ() == z >> 4;
    }

    /**
     * @param size      the generation size
     * @param minHeight the min height to generate at
     * @return the min height that can be populated based on the specified size and minHeight.
     */
    protected int getMinGenerationHeight(int size, int minHeight) {
        return minHeight;
    }

    /**
     * @param size      the generation size
     * @param maxHeight the max height to generate at
     * @return the max height that can be populated based on the specified size and maxHeight.
     */
    protected int getMaxGenerationHeight(int size, int maxHeight) {
        return maxHeight;
    }

    /**
     * @return the min height that can be populated
     */
    private int getMinGenerationHeight() {
        return Math.max(0, getMinGenerationHeight(size, minHeight));
    }

    /**
     * @return the max height that can be populated
     */
    private int getMaxGenerationHeight() {
        return Math.min(255, getMaxGenerationHeight(size, maxHeight));
    }
}
