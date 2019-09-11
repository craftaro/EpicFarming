package com.songoda.epicfarming.commands;

import com.songoda.core.commands.AbstractCommand;
import com.songoda.epicfarming.EpicFarming;
import com.songoda.epicfarming.boost.BoostData;
import com.songoda.epicfarming.utils.Methods;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class CommandBoost extends AbstractCommand {

    final EpicFarming instance;

    public CommandBoost(EpicFarming instance) {
        super(false, "boost");
        this.instance = instance;
    }

    @Override
    protected ReturnType runCommand(CommandSender sender, String... args) {
        if (args.length < 3) {
            return ReturnType.SYNTAX_ERROR;
        }
            if (Bukkit.getPlayer(args[1]) == null) {
                instance.getLocale().newMessage("&cThat player does not exist...").sendPrefixedMessage(sender);
                return ReturnType.FAILURE;
            } else if (!Methods.isInt(args[1])) {
                instance.getLocale().newMessage("&6" + args[1] + " &7is not a number...").sendPrefixedMessage(sender);
                return ReturnType.FAILURE;
            } else {
                Calendar c = Calendar.getInstance();
                Date currentDate = new Date();
                c.setTime(currentDate);

                String time = "&7.";

                if (args.length > 3) {
                    if (args[3].contains("m:")) {
                        String[] arr2 = (args[3]).split(":");
                        c.add(Calendar.MINUTE, Integer.parseInt(arr2[1]));
                        time = " &7for &6" + arr2[1] + " minutes&7.";
                    } else if (args[3].contains("h:")) {
                        String[] arr2 = (args[3]).split(":");
                        c.add(Calendar.HOUR, Integer.parseInt(arr2[1]));
                        time = " &7for &6" + arr2[1] + " hours&7.";
                    } else if (args[3].contains("d:")) {
                        String[] arr2 = (args[3]).split(":");
                        c.add(Calendar.HOUR, Integer.parseInt(arr2[1]) * 24);
                        time = " &7for &6" + arr2[1] + " days&7.";
                    } else if (args[3].contains("y:")) {
                        String[] arr2 = (args[3]).split(":");
                        c.add(Calendar.YEAR, Integer.parseInt(arr2[1]));
                        time = " &7for &6" + arr2[1] + " years&7.";
                    } else {
                        instance.getLocale().newMessage("&7" + args[2] + " &7is invalid.").sendPrefixedMessage(sender);
                        return ReturnType.SUCCESS;
                    }
                } else {
                    c.add(Calendar.YEAR, 10);
                }

                BoostData boostData = new BoostData(Integer.parseInt(args[2]), c.getTime().getTime(), Bukkit.getPlayer(args[1]).getUniqueId());
                instance.getBoostManager().addBoostToPlayer(boostData);
                instance.getLocale().newMessage("&7Successfully boosted &6" + Bukkit.getPlayer(args[0]).getName()
                        + "'s &7furnaces reward amounts by &6" + args[2] + "x" + time).sendPrefixedMessage(sender);
            }
        return ReturnType.FAILURE;
    }

    @Override
    protected List<String> onTab(CommandSender commandSender, String... strings) {
        return null;
    }

    @Override
    public String getPermissionNode() {
        return "epicfarming.admin";
    }

    @Override
    public String getSyntax() {
        return "/efa boost <player> <multiplier> [m:minute, h:hour, d:day, y:year]";
    }

    @Override
    public String getDescription() {
        return "This allows you to boost a players farms yield amounts by a multiplier (Put 2 for double, 3 for triple and so on).";
    }
}
