package fr.asvadia.guard.bungee.listener;

import fr.asvadia.api.bungee.messaging.event.RequestEvent;
import fr.asvadia.api.common.security.Totp;
import fr.asvadia.guard.common.player.GuardPlayer;
import fr.asvadia.guard.util.Notification;
import fr.asvadia.guard.bungee.ProxyMain;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.ArrayList;
import java.util.List;

public class ServerListener implements Listener {

    ProxyMain main;

    public ServerListener(ProxyMain main) {
        this.main = main;
    }


    @EventHandler
    public void onServerConnect(ServerConnectedEvent event) {
        GuardPlayer<ProxiedPlayer> guardPlayer = main.getPlayerManager().get(event.getPlayer());
        if(guardPlayer.hasAccess()) main.getRequestManager().request(event.getPlayer(), "access", Totp.getTOTPCode(guardPlayer.getToken()));
    }



    @EventHandler
    public void onRequestEvent(RequestEvent event) {
        if (event.getContent().get(0).equals("notify")) {
            Notification notification = Notification.valueOf(event.getContent().get(1));
            String name = event.getContent().get(2);
            List<String> args = new ArrayList<>();
            for (int i = 3; i < event.getContent().size(); i++) args.add(event.getContent().get(i));
            args.add(event.getPlayer().getServer().getInfo().getName());
            main.getNotifyChannel().send(notification, name, args.toArray(new String[args.size()]));
        }
    }
}
