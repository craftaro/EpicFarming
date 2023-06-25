package com.songoda.epicfarming.commands;

import com.songoda.core.commands.AbstractCommand;
import com.songoda.epicfarming.EpicFarming;
import com.songoda.epicfarming.farming.levels.Level;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommandGiveFarmItem extends AbstractCommand {
    private final EpicFarming plugin;

    public CommandGiveFarmItem(EpicFarming plugin) {
        super(CommandType.CONSOLE_OK, "givefarmitem");
        this.plugin = plugin;
    }

    @Override
    protected ReturnType runCommand(CommandSender sender, String... args) {
        if (args.length == 1) {
            return ReturnType.SYNTAX_ERROR;
        }

        Level level = this.plugin.getLevelManager().getLowestLevel();
        Player player;
        if (args.length != 0 && Bukkit.getPlayer(args[0]) == null) {
            this.plugin.getLocale().newMessage("&cThat player does not exist or is currently offline.").sendPrefixedMessage(sender);
            return ReturnType.FAILURE;
        } else if (args.length == 0) {
            if (!(sender instanceof Player)) {
                this.plugin.getLocale().newMessage("&cYou need to be a player to give a farm item to yourself.").sendPrefixedMessage(sender);
                return ReturnType.FAILURE;
            }
            player = (Player) sender;
        } else {
            player = Bukkit.getPlayer(args[0]);
        }


        if (args.length >= 2 && !this.plugin.getLevelManager().isLevel(Integer.parseInt(args[1]))) {
            this.plugin.getLocale().newMessage("&cNot a valid level... The current valid levels are: &4"
                    + this.plugin.getLevelManager().getLowestLevel().getLevel() + "-"
                    + this.plugin.getLevelManager().getHighestLevel().getLevel() + "&c.").sendPrefixedMessage(sender);
            return ReturnType.FAILURE;
        } else if (args.length != 0) {

            level = this.plugin.getLevelManager().getLevel(Integer.parseInt(args[1]));
        }
        player.getInventory().addItem(this.plugin.makeFarmItem(level));
        this.plugin.getLocale().getMessage("command.give.success")
                .processPlaceholder("level", level.getLevel()).sendPrefixedMessage(player);

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
        }
        return null;
    }

    @Override
    public String getPermissionNode() {
        return "epicfarming.give";
    }

    @Override
    public String getSyntax() {
        return "givefarmitem [player] <level>";
    }

    @Override
    public String getDescription() {
        return "Give a farm item to a player.";
    }
}
