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

import java.util.*;

/**
 * Generates blocks in clusters (or "veins").
 */
public class VeinPopulator extends Populator {

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
    public VeinPopulator(@NotNull Plugin plugin, @NotNull PopulatedChunksData populatedChunksData, @Nullable Material type,
                     @Nullable Collection<Material> types, int size, double triesPerChunk, int minHeight, int maxHeight,
                     @Nullable Material replaceWith, @Nullable Material replaceRestWith, @Nullable Set<Material> canReplace,
                     @NotNull Set<String> worlds, @Nullable Set<Biome> biomes) {
        super(plugin, populatedChunksData, type, types, size, triesPerChunk, minHeight, maxHeight, replaceWith,
                replaceRestWith, canReplace, worlds, biomes);
    }

    /**
     * Generates a vein of minerals.
     * <p>
     * This algorithm mimics the one used internally in Minecraft. The size variable does not represent an exact amount
     * of blocks to generate, but rather a rough estimate. Low values (~3), for example, will not produce any blocks.
     * The <a href="https://minecraft.gamepedia.com/Mineral_vein">wiki</a> provides some values that are used in vanilla.
     *
     * @param world       the world to generate in
     * @param sourceChunk the chunk to generate for
     * @param random      the random generator to use
     * @param size        the size of the generation (vein)
     * @param x           the X-coordinate of the origin block
     * @param y           the Y-coordinate of the origin block
     * @param z           the Z-coordinate of the origin block
     */
    @Override
    protected void generate(@NotNull World world, @NotNull Chunk sourceChunk, @NotNull Random random,
                            int size, int x, int y, int z) {
        double rpi = random.nextDouble() * Math.PI;

        double x1 = x + Math.sin(rpi) * size / 8.0f;
        double x2 = x - Math.sin(rpi) * size / 8.0f;
        double z1 = z + Math.cos(rpi) * size / 8.0f;
        double z2 = z - Math.cos(rpi) * size / 8.0f;

        double y1 = y + random.nextInt(3) - 2;
        double y2 = y + random.nextInt(3) - 2;

        for (int i = 0; i <= size; i++) {
            double xPos = x1 + (x2 - x1) * i / size;
            double yPos = y1 + (y2 - y1) * i / size;
            double zPos = z1 + (z2 - z1) * i / size;

            double fuzz = random.nextDouble() * size / 16.0d;
            double fuzzXZ = (Math.sin((float) (i * Math.PI / size)) + 1.0f) * fuzz + 1.0d;
            double fuzzY = (Math.sin((float) (i * Math.PI / size)) + 1.0f) * fuzz + 1.0d;

            int xStart = (int) Math.floor(xPos - fuzzXZ / 2.0d);
            int yStart = (int) Math.floor(yPos - fuzzY / 2.0d);
            int zStart = (int) Math.floor(zPos - fuzzXZ / 2.0d);

            int xEnd = (int) Math.floor(xPos + fuzzXZ / 2.0d);
            int yEnd = (int) Math.floor(yPos + fuzzY / 2.0d);
            int zEnd = (int) Math.floor(zPos + fuzzXZ / 2.0d);

            for (int ix = xStart; ix <= xEnd; ix++) {
                double xThresh = (ix + 0.5d - xPos) / (fuzzXZ / 2.0d);
                if (xThresh * xThresh < 1.0d) {
                    for (int iy = yStart; iy <= yEnd; iy++) {
                        double yThresh = (iy + 0.5d - yPos) / (fuzzY / 2.0d);
                        if (xThresh * xThresh + yThresh * yThresh < 1.0d) {
                            for (int iz = zStart; iz <= zEnd; iz++) {
                                double zThresh = (iz + 0.5d - zPos) / (fuzzXZ / 2.0d);
                                if (xThresh * xThresh + yThresh * yThresh + zThresh * zThresh < 1.0d) {

                                    setBlock(world, sourceChunk, random, ix, iy, iz);

                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    protected int getMinGenerationHeight(int size, int minHeight) {
        return minHeight - (size > 3 ? 3 : 0);
    }
}
