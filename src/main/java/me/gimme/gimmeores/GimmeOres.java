package me.gimme.gimmeores;

import me.gimme.gimmecore.command.BaseHelpCommand;
import me.gimme.gimmecore.command.CommandManager;
import me.gimme.gimmeores.chunk.ChunkManager;
import me.gimme.gimmeores.command.BaseCommand;
import me.gimme.gimmeores.command.commands.OresCountCommand;
import me.gimme.gimmeores.command.commands.OresPopulateCommand;
import org.bukkit.Material;
import org.bukkit.command.CommandExecutor;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;

public final class GimmeOres extends JavaPlugin implements CommandExecutor {
    public static final String CONFIG_DEBUG = "debug";

    public static final String PERMISSIONS_PATH = "gimmeores";
    public static final String ORES_COMMAND = "ores";

    private CommandManager commandManager = new CommandManager(this);
    private ChunkManager chunkManager = new ChunkManager(this);

    @Override
    public void onEnable() {
        getConfig().options().copyDefaults(true);
        saveDefaultConfig();

        registerCommands();
        registerEvents();

        chunkManager.onEnable();
    }

    @Override
    public void onDisable() {
        chunkManager.onDisable();
    }

    private void registerCommands() {
        commandManager.registerPlaceholder(BaseCommand.MATERIAL_PLACEHOLDER,
                Arrays.asList(Material.values()), (material) -> material.toString().toLowerCase());

        registerCommand(new BaseHelpCommand(commandManager, ORES_COMMAND, null, false) {});
        registerCommand(new OresCountCommand());
        registerCommand(new OresPopulateCommand(chunkManager));
    }

    private void registerEvents() {
        registerEvents(chunkManager);
    }

    private void registerCommand(me.gimme.gimmecore.command.BaseCommand command) {
        commandManager.register(command);
    }

    private void registerEvents(Listener listener) {
        getServer().getPluginManager().registerEvents(listener, this);
    }
}
