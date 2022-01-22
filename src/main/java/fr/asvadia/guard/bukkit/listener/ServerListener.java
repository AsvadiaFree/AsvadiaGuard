package fr.asvadia.guard.bukkit.listener;

import fr.asvadia.api.bukkit.messaging.event.RequestEvent;
import fr.asvadia.api.bukkit.messaging.request.Request;
import fr.asvadia.guard.bukkit.BukkitMain;
import fr.asvadia.guard.common.player.GuardPlayer;
import fr.asvadia.guard.util.Notification;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;

public class ServerListener implements Listener {

    BukkitMain main;

    public ServerListener(BukkitMain main) {
        this.main = main;
    }


    @EventHandler
    private void onDisablePlugin(PluginDisableEvent event) {
        if(event.getPlugin() != main) {
            main.getNotifyChannel().send(Notification.DISABLE_PLUGIN, event.getPlugin().getName());
        }
    }


    @EventHandler
    public void onRequestReceive(RequestEvent event) {
        if (event.getContent().get(0).equals("access")) {
            GuardPlayer<Player> player = main.getPlayerManager().get(event.getPlayer());
            if(player.getToken() != null) {
                if (player.access(event.getContent().get(1))) {
                    event.getPlayer().sendMessage((String) main.getConf().get("chat.protect.success"));
                } else event.getPlayer().sendMessage((String) main.getConf().get("chat.protect.failure"));
            } else event.getPlayer().sendMessage((String) main.getConf().get("chat.protect.noToken"));
        } if(event.getContent().get(0).equals("synchronisation")) {
            GuardPlayer<Player> player = main.getPlayerManager().get(event.getPlayer());
            Request request = new Request(event.getRequest().getId(), event.getPlayer());
            String result;
            if(main.hasSynchronisation()) {
                if (player.getToken() != null) {
                    player.setToken(event.getContent().get(1));
                    result = "success";
                } else result = "noToken";
            } else result = "disable";
            main.getRequestManager().request(request, "synchronisation", result);
        }
    }


}
