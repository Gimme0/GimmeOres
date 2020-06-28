package me.gimme.gimmeores.command.commands;

import me.gimme.gimmeores.command.BaseCommand;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OresGenerateCommand extends BaseCommand {
    public OresGenerateCommand() {
        super("generate");

        addAlias("gen");
        setArgsUsage("<chunk distance>");
        addArgsAlternative("0");
        setMinArgs(1);
        setMaxArgs(1);
        setPlayerOnly(true);
        setDescription("Generates surrounding chunks");
    }

    @Override
    protected @Nullable String execute(@NotNull CommandSender sender, @NotNull String[] args) {
        Player player = (Player) sender;

        int chunkDistance;
        try {
            chunkDistance = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            return errorMessageWithUsage(CommandError.NOT_A_NUMBER, args[0]);
        }

        sender.sendMessage("Generating chunks...");

        Chunk playerChunk = player.getLocation().getChunk();

        int chunksGenerated = 0;
        for (int cx = -chunkDistance; cx <= chunkDistance; cx++) {
            for (int cz = -chunkDistance; cz <= chunkDistance; cz++) {
                if (!player.getWorld().isChunkLoaded(playerChunk.getX() + cx, playerChunk.getZ() + cz)) {
                    player.getWorld().loadChunk(playerChunk.getX() + cx, playerChunk.getZ() + cz);
                }
            }
        }

        return "Generated " + chunksGenerated + " chunks";
    }
}
