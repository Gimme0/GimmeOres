package me.gimme.gimmeores.command.commands;

import me.gimme.gimmeores.chunk.ChunkManager;
import me.gimme.gimmeores.command.BaseCommand;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OresPopulateCommand extends BaseCommand {
    private ChunkManager chunkManager;

    public OresPopulateCommand(@NotNull ChunkManager chunkManager) {
        super("populate");

        addAlias("pop");
        setArgsUsage("<chunk distance>");
        addArgsAlternative("0");
        setMinArgs(1);
        setMaxArgs(1);
        setPlayerOnly(true);
        setDescription("Populates surrounding chunks");

        this.chunkManager = chunkManager;
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

        Chunk playerChunk = player.getLocation().getChunk();

        // Load all chunks
        forEachChunk(player.getWorld(), playerChunk.getX(), playerChunk.getZ(), chunkDistance, World::loadChunk);
        // Unload all chunks
        forEachChunk(player.getWorld(), playerChunk.getX(), playerChunk.getZ(), chunkDistance, (World world, Chunk chunk) -> {
            world.setChunkForceLoaded(chunk.getX(), chunk.getZ(), false);
        });

        int chunksSide = 1 + chunkDistance * 2;
        double chunksPopulated = chunksSide * chunksSide;

        return "Populating " + chunksPopulated + " chunks";
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
