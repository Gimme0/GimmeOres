package me.gimme.gimmeores.command;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class CommandManager implements CommandExecutor {
    public static final Set<String> COMMANDS = new HashSet<>(Arrays.asList("ores", "chunks"));

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;

            Material material = Material.matchMaterial(args[0]);
            int size = Integer.parseInt(args[1]);

            Chunk playerChunk = player.getLocation().getChunk();

            if ("ores".equals(label)) {
                int chunkCount = 0;
                int count = 0;
                int[] heightCount = new int[256];

                for (int xx = -size; xx <= size; xx++) {
                    for (int zz = -size; zz <= size; zz++) {
                        if (!player.getWorld().isChunkLoaded(playerChunk.getX() + xx, playerChunk.getZ() + zz)) {
                            player.getWorld().loadChunk(playerChunk.getX() + xx, playerChunk.getZ() + zz);
                        }
                    }
                }

                for (int xx = -size; xx <= size; xx++) {
                    for (int zz = -size; zz <= size; zz++) {
                        Chunk chunk = player.getWorld().getChunkAt(playerChunk.getX() + xx, playerChunk.getZ() + zz);
                        chunkCount++;

                        for (int x = 0; x < 16; x++) {
                            for (int z = 0; z < 16; z++) {
                                for (int y = 0; y < 256; y++) {
                                    Block block = chunk.getBlock(x, y, z);
                                    if (!block.getType().equals(material)) continue;

                                    count++;
                                    heightCount[y] = heightCount[y] + 1;
                                }
                            }
                        }
                    }
                }

                player.sendMessage("--------------------");
                player.sendMessage(count + " " + material + " in " + chunkCount + " chunks" + (size == 0 ? " (" + player.getWorld().getBiome(player.getLocation().getBlockX(),
                        player.getLocation().getBlockY(), player.getLocation().getBlockZ()) + ")" : ""));
                for (int y = 0; y < heightCount.length; y++) {
                    if (heightCount[y] == 0) continue;
                    player.sendMessage("y = " + y + ": " + heightCount[y] + " (" + (heightCount[y] * 100 / count) + "%)");
                }

                return true;
            }
        }

        return false;
    }
}
