package com.songoda.epicfarming.commands;

import com.songoda.epicfarming.EpicFarming;
import com.songoda.epicfarming.command.AbstractCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandSettings extends AbstractCommand {

    public CommandSettings(AbstractCommand parent) {
        super("settings", parent, true);
    }

    @Override
    protected ReturnType runCommand(EpicFarming instance, CommandSender sender, String... args) {
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
