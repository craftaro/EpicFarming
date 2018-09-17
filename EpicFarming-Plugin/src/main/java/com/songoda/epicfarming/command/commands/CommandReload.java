package com.songoda.epicfarming.command.commands;

import com.songoda.arconix.api.methods.formatting.TextComponent;
import com.songoda.epicfarming.EpicFarmingPlugin;
import com.songoda.epicfarming.command.AbstractCommand;
import org.bukkit.command.CommandSender;

public class CommandReload extends AbstractCommand {

    public CommandReload(AbstractCommand parent) {
        super("reload", parent, false);
    }

    @Override
    protected ReturnType runCommand(EpicFarmingPlugin instance, CommandSender sender, String... args) {
        instance.reload();
        sender.sendMessage(TextComponent.formatText(instance.getReferences().getPrefix() + "&7Configuration and Language files reloaded."));
        return ReturnType.SUCCESS;
    }

    @Override
    public String getPermissionNode() {
        return "epicfarming.admin";
    }

    @Override
    public String getSyntax() {
        return "/efa reload";
    }

    @Override
    public String getDescription() {
        return "Reload the Configuration and Language files.";
    }
}
