package com.songoda.epicfarming.command.commands;

import com.songoda.arconix.api.methods.formatting.TextComponent;
import com.songoda.arconix.api.methods.math.AMath;
import com.songoda.epicfarming.EpicFarmingPlugin;
import com.songoda.epicfarming.boost.BoostData;
import com.songoda.epicfarming.command.AbstractCommand;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.Calendar;
import java.util.Date;

public class CommandBoost extends AbstractCommand {

    public CommandBoost(AbstractCommand parent) {
        super("boost", parent, false);
    }

    @Override
    protected ReturnType runCommand(EpicFarmingPlugin instance, CommandSender sender, String... args) {
        if (args.length < 3) {
            return ReturnType.SYNTAX_ERROR;
        }
            if (Bukkit.getPlayer(args[1]) == null) {
                sender.sendMessage(TextComponent.formatText(instance.getReferences().getPrefix() + "&cThat player does not exist..."));
                return ReturnType.FAILURE;
            } else if (!AMath.isInt(args[2])) {
                sender.sendMessage(TextComponent.formatText(instance.getReferences().getPrefix() + "&6" + args[2] + " &7is not a number..."));
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
                        sender.sendMessage(TextComponent.formatText(instance.getReferences().getPrefix() + "&7" + args[3] + " &7is invalid."));
                        return ReturnType.SUCCESS;
                    }
                } else {
                    c.add(Calendar.YEAR, 10);
                }

                BoostData boostData = new BoostData(Integer.parseInt(args[2]), c.getTime().getTime(), Bukkit.getPlayer(args[1]).getUniqueId());
                instance.getBoostManager().addBoostToPlayer(boostData);
                sender.sendMessage(TextComponent.formatText(instance.getReferences().getPrefix() + "&7Successfully boosted &6" + Bukkit.getPlayer(args[1]).getName() + "'s &7farms yield rates by &6" + args[2] + "x" + time));
            }
        return ReturnType.FAILURE;
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
