package com.songoda.epicfarming.commands;

import com.songoda.core.commands.AbstractCommand;
import com.songoda.core.utils.NumberUtils;
import com.songoda.core.utils.TimeUtils;
import com.songoda.epicfarming.EpicFarming;
import com.songoda.epicfarming.boost.BoostData;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommandBoost extends AbstractCommand {
    private final EpicFarming plugin;

    public CommandBoost(EpicFarming plugin) {
        super(CommandType.CONSOLE_OK, "boost");
        this.plugin = plugin;
    }

    @Override
    protected ReturnType runCommand(CommandSender sender, String... args) {
        if (args.length < 2) {
            this.plugin.getLocale().newMessage("&7Syntax error...").sendPrefixedMessage(sender);
            return ReturnType.SYNTAX_ERROR;
        }
        if (!NumberUtils.isInt(args[1])) {
            this.plugin.getLocale().newMessage("&6" + args[1] + " &7is not a number...").sendPrefixedMessage(sender);
            return ReturnType.SYNTAX_ERROR;
        }

        long duration = 0L;

        if (args.length > 2) {
            for (String line : args) {
                long time = TimeUtils.parseTime(line);
                duration += time;
            }
        }

        Player player = Bukkit.getPlayer(args[0]);
        if (player == null) {
            this.plugin.getLocale().newMessage("&cThat player does not exist or is not online...").sendPrefixedMessage(sender);
            return ReturnType.FAILURE;
        }

        BoostData boostData = new BoostData(Integer.parseInt(args[1]), duration == 0L ? Long.MAX_VALUE : System.currentTimeMillis() + duration, player.getUniqueId());
        this.plugin.getDataManager().createBoost(boostData);
        this.plugin.getBoostManager().addBoostToPlayer(boostData);
        this.plugin.getLocale().newMessage("&7Successfully boosted &6" + Bukkit.getPlayer(args[0]).getName()
                        + "'s &7farms by &6" + args[1] + "x" + (duration == 0L ? "" : (" for " + TimeUtils.makeReadable(duration))) + "&7.")
                .sendPrefixedMessage(sender);
        return ReturnType.SUCCESS;
    }

    @Override
    protected List<String> onTab(CommandSender sender, String... args) {
        if (args.length == 1) {
            List<String> players = new ArrayList<>();
            for (Player player : Bukkit.getOnlinePlayers()) {
                players.add(player.getName());
            }
            return players;
        } else if (args.length == 2) {
            return Arrays.asList("1", "2", "3", "4", "5");
        } else if (args.length == 3) {
            return Arrays.asList("1m", "1h", "1d");
        }
        return null;
    }

    @Override
    public String getPermissionNode() {
        return "epicfarming.boost";
    }

    @Override
    public String getSyntax() {
        return "boost <player> <amount> [duration]";
    }

    @Override
    public String getDescription() {
        return "This allows you to boost a players farms yield amounts by a multiplier (Put 2 for double, 3 for triple and so on).";
    }
}
