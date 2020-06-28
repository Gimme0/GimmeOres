package me.gimme.gimmeores;

import me.gimme.gimmeores.chunk.ChunkManager;
import me.gimme.gimmeores.command.CommandManager;
import org.bukkit.command.CommandExecutor;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class GimmeOres extends JavaPlugin implements CommandExecutor {

    private ChunkManager chunkManager = new ChunkManager(this);

    @Override
    public void onEnable() {
        getConfig().options().copyDefaults(true);
        saveDefaultConfig();

        CommandManager commandManager = new CommandManager();
        CommandManager.COMMANDS.stream()
                .map(this::getCommand)
                .filter(Objects::nonNull)
                .forEach(command -> command.setExecutor(commandManager));

        registerEvents(chunkManager);

        chunkManager.onEnable();
    }

    @Override
    public void onDisable() {
        chunkManager.onDisable();
    }

    private void registerEvents(Listener listener) {
        getServer().getPluginManager().registerEvents(listener, this);
    }
}
