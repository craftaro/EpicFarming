package com.songoda.epicfarming.commands;

import com.songoda.epicfarming.EpicFarming;
import com.songoda.epicfarming.command.AbstractCommand;
import com.songoda.epicfarming.utils.Methods;
import org.bukkit.command.CommandSender;

public class CommandEpicFarming extends AbstractCommand {

    public CommandEpicFarming() {
        super("EpicFarming", null, false);
    }

    @Override
    protected ReturnType runCommand(EpicFarming instance, CommandSender sender, String... args) {
        sender.sendMessage("");
        sender.sendMessage(Methods.formatText(instance.getReferences().getPrefix() + "&7Version " + instance.getDescription().getVersion() + " Created with <3 by &5&l&oBrianna"));

        for (AbstractCommand command : instance.getCommandManager().getCommands()) {
            if (command.getPermissionNode() == null || sender.hasPermission(command.getPermissionNode())) {
                sender.sendMessage(Methods.formatText("&8 - &a" + command.getSyntax() + "&7 - " + command.getDescription()));
            }
        }
        sender.sendMessage("");

        return ReturnType.SUCCESS;
    }

    @Override
    public String getPermissionNode() {
        return null;
    }

    @Override
    public String getSyntax() {
        return "/EpicFarming";
    }

    @Override
    public String getDescription() {
        return "Displays this page.";
    }
}
