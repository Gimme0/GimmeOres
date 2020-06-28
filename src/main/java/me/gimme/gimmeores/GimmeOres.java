package me.gimme.gimmeores;

import me.gimme.gimmecore.command.BaseCommand;
import me.gimme.gimmeores.chunk.ChunkManager;
import me.gimme.gimmeores.command.commands.OresCountCommand;
import me.gimme.gimmeores.command.commands.OresPopulateCommand;
import org.bukkit.Material;
import org.bukkit.command.CommandExecutor;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;

public final class GimmeOres extends JavaPlugin implements CommandExecutor {
    public static final String PERMISSIONS_PATH = "gimmeores";
    public static final String ORES_COMMAND = "ores";

    private me.gimme.gimmecore.command.CommandManager commandManager = new me.gimme.gimmecore.command.CommandManager(this);
    private ChunkManager chunkManager = new ChunkManager(this);

    @Override
    public void onEnable() {
        getConfig().options().copyDefaults(true);
        saveDefaultConfig();

        //CommandManager commandManager = new CommandManager(); // TODO: remove
        //CommandManager.COMMANDS.stream()
        //        .map(this::getCommand)
        //        .filter(Objects::nonNull)
        //        .forEach(command -> command.setExecutor(commandManager));

        registerEvents(chunkManager);

        chunkManager.onEnable();
    }

    @Override
    public void onDisable() {
        chunkManager.onDisable();
    }

    private void registerCommands() {
        commandManager.registerPlaceholder(me.gimme.gimmeores.command.BaseCommand.MATERIAL_PLACEHOLDER,
                Arrays.asList(Material.values()), (material) -> material.toString().toLowerCase());

        registerCommand(new OresCountCommand());
        registerCommand(new OresPopulateCommand(chunkManager));
    }

    private void registerCommand(BaseCommand command) {
        commandManager.register(command);
    }

    private void registerEvents(Listener listener) {
        getServer().getPluginManager().registerEvents(listener, this);
    }
}
