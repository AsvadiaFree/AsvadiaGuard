package fr.asvadia.guard.bungee.command;

import fr.asvadia.guard.bukkit.object.PlayerManager;
import fr.asvadia.guard.bungee.ProxyMain;
import fr.asvadia.guard.common.player.GuardPlayer;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.command.ConsoleCommandSender;

public class GuardCommand extends Command {

    ProxyMain main;

    public GuardCommand(ProxyMain main, String name) {
        super(name);
        this.main = main;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(sender instanceof ConsoleCommandSender) {
            if(args.length > 0) {
                ProxiedPlayer player = ProxyServer.getInstance().getPlayer(args[0]);
                if(player != null) {
                    GuardPlayer<ProxiedPlayer> guardPlayer = main.getPlayerManager().get(player);
                    if(guardPlayer.getToken() != null) {
                        sender.sendMessage(TextComponent.fromLegacyText(guardPlayer.getToken()));
                        return;
                    }
                }
                if(args[0].equalsIgnoreCase("reload")) reload((ConsoleCommandSender) sender);
            }
        } else {
            GuardPlayer<ProxiedPlayer> guardPlayer = main.getPlayerManager().get((ProxiedPlayer) sender);
            if(guardPlayer.getToken() != null) {
                if (args.length > 0) {
                    if (args[0].equalsIgnoreCase("synchronisation") || args[0].equalsIgnoreCase("synch") || args[0].equalsIgnoreCase("s")) {
                        synchronisation(guardPlayer);
                        return;
                    } else {
                        access(guardPlayer, args[0]);
                    }
                }
            } else sender.sendMessage(TextComponent.fromLegacyText(main.getConf().get("chat.permission")));
        }
    }


    public void reload(ConsoleCommandSender sender) {
        main.getData().save();
        main.getConf().load();
        main.getData().load();

        main.getPlayerManager().getMap().clear();
        ProxyServer.getInstance().getPlayers().forEach(p -> main.getPlayerManager().load(p));
        sender.sendMessage(TextComponent.fromLegacyText("Reload success"));
    }

    public void access(GuardPlayer<ProxiedPlayer> player, String code) {
        if(player.access(code)) player.getPlayer().sendMessage(TextComponent.fromLegacyText(main.getConf().get("chat.protect.success")));
        else player.getPlayer().sendMessage(TextComponent.fromLegacyText(main.getConf().get("chat.protect.failure")));
        main.getRequestManager().request(player.getPlayer(), "access", code);
    }

    public void synchronisation(GuardPlayer<ProxiedPlayer> guardPlayer) {
        if(main.hasSynchronisation()) {
            if (guardPlayer.hasAccess()) {
                guardPlayer.getPlayer().sendMessage(TextComponent.fromLegacyText(main.getConf().get("chat.protect.synchronisation.start")));
                main.getRequestManager().request(request -> {
                    if (request != null) {
                        String key = request.getContent().get(1);
                        if (key.equals("noToken")) guardPlayer.getPlayer().sendMessage(TextComponent.fromLegacyText(main.getConf().get("chat.protect.synchronisation.noToken")));
                        else guardPlayer.getPlayer().sendMessage(TextComponent.fromLegacyText(main.getConf().get("chat.protect.synchronisation.success")));
                    } else
                        guardPlayer.getPlayer().sendMessage(TextComponent.fromLegacyText(main.getConf().get("chat.protect.synchronisation.timeout")));
                }, guardPlayer.getPlayer(), "synchronisation", guardPlayer.getToken());
            } else guardPlayer.getPlayer().sendMessage(TextComponent.fromLegacyText(main.getConf().get("chat.protect.notAccess")));
        } else guardPlayer.getPlayer().sendMessage(TextComponent.fromLegacyText(main.getConf().get("chat.protect.synchronisation.disable")));
    }


}
