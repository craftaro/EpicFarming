package com.songoda.epicfarming.handlers;

import com.songoda.arconix.plugin.Arconix;
import com.songoda.epicfarming.EpicFarming;
import com.songoda.epicfarming.utils.Debugger;
import com.songoda.epicfarming.utils.Methods;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Created by songoda on 3/14/2017.
 */

public class CommandHandler implements CommandExecutor {

    private EpicFarming instance;

    public CommandHandler(EpicFarming instance) {
        this.instance = instance;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        try {
            if (args.length == 0 || args[0].equalsIgnoreCase("help") || args[0].equalsIgnoreCase("?")) {
                sender.sendMessage("");
                sender.sendMessage(Arconix.pl().getApi().format().formatText(instance.references.getPrefix() + "&7" + instance.getDescription().getVersion() + " Created by &5&l&oBrianna"));
                sender.sendMessage(Arconix.pl().getApi().format().formatText(" &8- &aEFA help &7Displays this page."));
                sender.sendMessage(Arconix.pl().getApi().format().formatText(" &8- &aEFA settings &7Edit the EpicFarming Settings."));
                sender.sendMessage(Arconix.pl().getApi().format().formatText(" &8- &aEFA reload &7Reloads Configuration and Language files."));
                sender.sendMessage(Arconix.pl().getApi().format().formatText(" &8- &aEFA givefarmitem [player] &7Give a farm item to a player."));
                sender.sendMessage("");
            } else if (args[0].equalsIgnoreCase("reload")) {
                if (!sender.hasPermission("epicfarming.admin")) {
                    sender.sendMessage(instance.references.getPrefix() + instance.getLocale().getMessage("event.general.nopermission"));
                } else {
                    instance.reload();
                    sender.sendMessage(Arconix.pl().getApi().format().formatText(instance.references.getPrefix() + "&8Configuration and Language files reloaded."));
                }
            } else if (args[0].equalsIgnoreCase("givefarmitem")) {
                if (!sender.hasPermission("epicfarming.admin")) {
                    sender.sendMessage(instance.references.getPrefix() + instance.getLocale().getMessage("event.general.nopermission"));
                    return true;
                }
                //ToDo: add the ability to specify level.
                if (args.length == 1) {
                    if (sender instanceof Player)
                        ((Player) sender).getInventory().addItem(Methods.makeFarmItem(1));
                } else if (Bukkit.getPlayerExact(args[1]) == null) {
                    sender.sendMessage(instance.references.getPrefix() + Arconix.pl().getApi().format().formatText("&cThat username does not exist, or the user is not online!"));
                } else {
                    Bukkit.getPlayerExact(args[1]).getInventory().addItem(Methods.makeFarmItem(1));
                }
            } else if (sender instanceof Player) {
                if (args[0].equalsIgnoreCase("settings")) {
                    if (!sender.hasPermission("epicfarming.admin")) {
                        sender.sendMessage(instance.references.getPrefix() + instance.getLocale().getMessage("event.general.nopermission"));
                    } else {
                        Player p = (Player) sender;
                        instance.settingsManager.openSettingsManager(p);
                    }
                }
            }

        } catch (Exception ex) {
            Debugger.runReport(ex);
        }
        return true;
    }
}