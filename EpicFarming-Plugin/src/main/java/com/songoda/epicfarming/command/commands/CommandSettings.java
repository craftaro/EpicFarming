package com.songoda.epicfarming.command.commands;

import com.songoda.epicfarming.EpicFarmingPlugin;
import com.songoda.epicfarming.command.AbstractCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandSettings extends AbstractCommand {

    public CommandSettings(AbstractCommand parent) {
        super("settings", parent, true);
    }

    @Override
    protected ReturnType runCommand(EpicFarmingPlugin instance, CommandSender sender, String... args) {
        instance.getSettingsManager().openSettingsManager((Player)sender);
        return ReturnType.SUCCESS;
    }

    @Override
    public String getPermissionNode() {
        return "epicfarming.admin";
    }

    @Override
    public String getSyntax() {
        return "/efa settings";
    }

    @Override
    public String getDescription() {
        return "Edit the EpicFarming Settings.";
    }
}
