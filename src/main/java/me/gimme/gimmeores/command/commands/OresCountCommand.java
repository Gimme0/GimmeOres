package me.gimme.gimmeores.command.commands;

import me.gimme.gimmeores.command.BaseCommand;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;
import java.util.concurrent.atomic.AtomicInteger;

public class OresCountCommand extends BaseCommand {
    public OresCountCommand() {
        super("count");

        addAlias("c");
        setArgsUsage("<block> <chunk distance>");
        addArgsAlternative(BaseCommand.MATERIAL_PLACEHOLDER + " " + 0);
        setMinArgs(2);
        setMaxArgs(2);
        setPlayerOnly(true);
        setDescription("Counts number of blocks of a type in surrounding chunks");
    }

    @Override
    protected @Nullable String execute(@NotNull CommandSender sender, @NotNull String[] args) {
        Player player = (Player) sender;

        Material material = Material.matchMaterial(args[0]);
        if (material == null) return errorMessageWithUsage("Not a valid material: " + args[0]);

        int chunkDistance;
        try {
            chunkDistance = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            return errorMessageWithUsage(CommandError.NOT_A_NUMBER, args[1]);
        }

        Chunk playerChunk = player.getLocation().getChunk();

        final AtomicInteger chunkCount = new AtomicInteger(0);
        final AtomicInteger blockCount = new AtomicInteger(0);
        final int[] heightCount = new int[256];

        forEachChunk(player.getWorld(), playerChunk.getX(), playerChunk.getZ(), chunkDistance, World::loadChunk);

        forEachChunk(player.getWorld(), playerChunk.getX(), playerChunk.getZ(), chunkDistance, (World world, Chunk chunk) -> {
            chunkCount.incrementAndGet();

            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    for (int y = 0; y < 256; y++) {
                        Block block = chunk.getBlock(x, y, z);
                        if (!block.getType().equals(material)) continue;

                        blockCount.incrementAndGet();
                        heightCount[y] = heightCount[y] + 1;
                    }
                }
            }
        });

        forEachChunk(player.getWorld(), playerChunk.getX(), playerChunk.getZ(), chunkDistance, (World world, Chunk chunk) -> {
            world.setChunkForceLoaded(chunk.getX(), chunk.getZ(), false);
        });

        return getFormattedMessage(player, material, chunkDistance, blockCount.get(), chunkCount.get(), heightCount);
    }

    @NotNull
    private String getFormattedMessage(@NotNull Player player, @NotNull Material material, int chunkDistance, int blockCount, int chunkCount, int[] heightCount) {
        StringBuilder sb = new StringBuilder();

        sb.append("--------------------").append(newLine);
        sb.append(blockCount + " " + material + " in " + chunkCount + " chunks"
                + (chunkDistance == 0 ? " (" + player.getWorld().getBiome(player.getLocation().getBlockX(),
                player.getLocation().getBlockY(), player.getLocation().getBlockZ()) + ")" : ""));

        DecimalFormat df = new DecimalFormat();
        df.setMinimumFractionDigits(1);
        df.setMaximumFractionDigits(1);

        for (int y = 0; y < heightCount.length; y++) {
            if (heightCount[y] == 0) continue;
            sb.append(newLine);
            sb.append("y = " + y + ": " + heightCount[y] + " (" + df.format(heightCount[y] * 100f / blockCount) + "%)");
        }

        return sb.toString();
    }

    private void forEachChunk(@NotNull World world, int sourceX, int sourceZ, int chunkDistance, @NotNull ForChunk forChunk) {
        for (int x = -chunkDistance; x <= chunkDistance; x++) {
            for (int z = -chunkDistance; z <= chunkDistance; z++) {
                forChunk.apply(world, world.getChunkAt(sourceX + x, sourceZ + z));
            }
        }
    }

    private interface ForChunk {
        void apply(@NotNull World world, @NotNull Chunk chunk);
    }
}
