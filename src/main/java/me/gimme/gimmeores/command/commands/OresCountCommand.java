package me.gimme.gimmeores.command.commands;

import me.gimme.gimmeores.command.BaseCommand;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;

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

        int chunkCount = 0;
        int blockCount = 0;
        int[] heightCount = new int[256];

        for (int cx = -chunkDistance; cx <= chunkDistance; cx++) {
            for (int cz = -chunkDistance; cz <= chunkDistance; cz++) {
                if (!player.getWorld().isChunkLoaded(playerChunk.getX() + cx, playerChunk.getZ() + cz)) {
                    player.getWorld().loadChunk(playerChunk.getX() + cx, playerChunk.getZ() + cz);
                }
            }
        }

        for (int cx = -chunkDistance; cx <= chunkDistance; cx++) {
            for (int cz = -chunkDistance; cz <= chunkDistance; cz++) {
                Chunk chunk = player.getWorld().getChunkAt(playerChunk.getX() + cx, playerChunk.getZ() + cz);
                chunkCount++;

                for (int x = 0; x < 16; x++) {
                    for (int z = 0; z < 16; z++) {
                        for (int y = 0; y < 256; y++) {
                            Block block = chunk.getBlock(x, y, z);
                            if (!block.getType().equals(material)) continue;

                            blockCount++;
                            heightCount[y] = heightCount[y] + 1;
                        }
                    }
                }
            }
        }

        return getFormattedMessage(player, material, chunkDistance, blockCount, chunkCount, heightCount);
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
}
