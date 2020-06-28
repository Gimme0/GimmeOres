package me.gimme.gimmeores.command;

import me.gimme.gimmeores.GimmeOres;
import org.jetbrains.annotations.NotNull;

public abstract class BaseCommand extends me.gimme.gimmecore.command.BaseCommand {
    public static final String MATERIAL_PLACEHOLDER = "%material%";

    protected BaseCommand(@NotNull String name) {
        super(GimmeOres.ORES_COMMAND, name);
        setPermission(GimmeOres.PERMISSIONS_PATH + "." + GimmeOres.ORES_COMMAND + "." + name);
    }
}
