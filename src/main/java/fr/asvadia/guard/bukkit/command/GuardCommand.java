package fr.asvadia.guard.bukkit.command;

import fr.asvadia.guard.bukkit.BukkitMain;
import fr.asvadia.guard.common.player.GuardPlayer;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

public class GuardCommand implements CommandExecutor {

    BukkitMain main;

    public GuardCommand(BukkitMain main) {
        this.main = main;
    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(sender instanceof ConsoleCommandSender) {
            if(args.length > 0) {
                Player player = Bukkit.getPlayer(args[0]);
                if(player != null) {
                    GuardPlayer<Player> guardPlayer = main.getPlayerManager().get(player);
                    if(guardPlayer.getToken() != null) {
                        sender.sendMessage(main.getPlayerManager().get(player).getToken());
                        return false;
                    }
                }
                if(args[0].equalsIgnoreCase("reload")) reload((ConsoleCommandSender) sender);
            }
        } else {
            GuardPlayer<Player> player = main.getPlayerManager().get((Player) sender);
            if(player.getToken() != null) {
                if (args.length > 0) {
                    if (!player.hasAccess()) access(player, args[0]);
                }
            } else sender.sendMessage((String) main.getConf().get("chat.permission"));
        }
        return false;
    }

    public void reload(ConsoleCommandSender sender) {
        main.getData().save();
        main.getConf().load();
        main.getData().load();

        main.getPlayerManager().getMap().clear();
        Bukkit.getOnlinePlayers().forEach(p -> main.getPlayerManager().load(p));
        sender.sendMessage("Reload success");
    }

    public void access(GuardPlayer<Player> player, String code) {
        if(player.access(code)) player.getPlayer().sendMessage((String) main.getConf().get("chat.protect.success"));
        else player.getPlayer().sendMessage((String) main.getConf().get("chat.protect.failure"));
    }

}
