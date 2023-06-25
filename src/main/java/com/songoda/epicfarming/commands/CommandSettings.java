package com.songoda.epicfarming.commands;

import com.songoda.core.commands.AbstractCommand;
import com.songoda.core.configuration.editor.PluginConfigGui;
import com.songoda.epicfarming.EpicFarming;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class CommandSettings extends AbstractCommand {
    private final EpicFarming plugin;

    public CommandSettings(EpicFarming plugin) {
        super(CommandType.PLAYER_ONLY, "settings");
        this.plugin = plugin;
    }

    @Override
    protected ReturnType runCommand(CommandSender sender, String... args) {
        this.plugin.getGuiManager().showGUI((Player) sender, new PluginConfigGui(this.plugin));
        return ReturnType.SUCCESS;
    }

    @Override
    protected List<String> onTab(CommandSender sender, String... args) {
        return null;
    }

    @Override
    public String getPermissionNode() {
        return "epicfarming.settings";
    }

    @Override
    public String getSyntax() {
        return "settings";
    }

    @Override
    public String getDescription() {
        return "Edit the EpicFarming Settings.";
    }
}
