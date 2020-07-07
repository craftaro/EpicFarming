package com.songoda.epicfarming.commands;

import com.songoda.core.commands.AbstractCommand;
import com.songoda.epicfarming.EpicFarming;
import org.bukkit.command.CommandSender;

import java.util.List;

public class CommandReload extends AbstractCommand {

    final EpicFarming instance;

    public CommandReload(EpicFarming instance) {
        super(false, "reload");
        this.instance = instance;
    }

    @Override
    protected ReturnType runCommand(CommandSender sender, String... args) {
        instance.reloadConfig();
        instance.getLocale().getMessage("&7Configuration and Language files reloaded.").sendPrefixedMessage(sender);
        return AbstractCommand.ReturnType.SUCCESS;
    }

    @Override
    protected List<String> onTab(CommandSender commandSender, String... strings) {
        return null;
    }

    @Override
    public String getPermissionNode() {
        return "epicfarming.reload";
    }

    @Override
    public String getSyntax() {
        return "reload";
    }

    @Override
    public String getDescription() {
        return "Reload the Configuration and Language files.";
    }
}
