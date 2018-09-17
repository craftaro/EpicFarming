package com.songoda.epicfarming.command.commands;

import com.sk89q.worldedit.internal.expression.runtime.Return;
import com.songoda.arconix.api.methods.formatting.TextComponent;
import com.songoda.arconix.plugin.Arconix;
import com.songoda.epicfarming.EpicFarmingPlugin;
import com.songoda.epicfarming.command.AbstractCommand;
import com.songoda.epicfarming.farming.ELevel;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandGiveFarmItem extends AbstractCommand {

    public CommandGiveFarmItem(AbstractCommand parent) {
        super("givefarmitem", parent, false);
    }

    @Override
    protected ReturnType runCommand(EpicFarmingPlugin instance, CommandSender sender, String... args) {
        if (args.length == 2) return ReturnType.SYNTAX_ERROR;

            ELevel level = instance.getLevelManager().getLowestLevel();
            Player player;
            if (args.length != 1 && Bukkit.getPlayer(args[1]) == null) {
                sender.sendMessage(instance.getReferences().getPrefix() + Arconix.pl().getApi().format().formatText("&cThat player does not exist or is currently offline."));
                return ReturnType.FAILURE;
            } else if (args.length == 1) {
                if (!(sender instanceof Player)) {
                    sender.sendMessage(instance.getReferences().getPrefix() + Arconix.pl().getApi().format().formatText("&cYou need to be a player to give a farm item to yourself."));
                    return ReturnType.FAILURE;
                }
                player = (Player)sender;
            } else {
                player = Bukkit.getPlayer(args[1]);
            }


            if (args.length >= 3 && !instance.getLevelManager().isLevel(Integer.parseInt(args[2]))) {
                sender.sendMessage(instance.getReferences().getPrefix() + Arconix.pl().getApi().format().formatText("&cNot a valid level... The current valid levels are: &4" + instance.getLevelManager().getLowestLevel().getLevel() + "-" + instance.getLevelManager().getHighestLevel().getLevel() + "&c."));
                return ReturnType.FAILURE;
            } else if (args.length != 1){

                level = instance.getLevelManager().getLevel(Integer.parseInt(args[2]));
            }
            player.getInventory().addItem(instance.makeFarmItem(level));
            player.sendMessage(instance.getReferences().getPrefix() + instance.getLocale().getMessage("command.give.success", level.getLevel()));

        return ReturnType.SUCCESS;
    }

    @Override
    public String getPermissionNode() {
        return "epicfarming.admin";
    }

    @Override
    public String getSyntax() {
        return "/efa givefarmitem [player] <level>";
    }

    @Override
    public String getDescription() {
        return "Give a farm item to a player.";
    }
}
