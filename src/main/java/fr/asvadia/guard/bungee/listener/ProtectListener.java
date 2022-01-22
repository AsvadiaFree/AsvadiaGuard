package fr.asvadia.guard.bungee.listener;

import fr.asvadia.guard.bungee.ProxyMain;
import fr.asvadia.guard.common.player.GuardPlayer;
import fr.asvadia.guard.util.Notification;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.event.TabCompleteEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

public class ProtectListener implements Listener {

    ProxyMain main;

    public ProtectListener(ProxyMain main) {
        this.main = main;
    }

    @EventHandler
    public void onTab(TabCompleteEvent event) {
        GuardPlayer<ProxiedPlayer> guardPlayer = main.getPlayerManager().get((ProxiedPlayer) event.getSender());
        if(guardPlayer != null) {
            if(!guardPlayer.getPlayer().hasPermission("guard.show") || !guardPlayer.hasAccess())
                event.getSuggestions().removeIf(suggestion -> main.getCommandManager().isHideCommand(suggestion));
        } else event.getSender().disconnect(TextComponent.fromLegacyText(main.getConf().get("chat.protect.incorrectSession")));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCommand(ChatEvent event) {
        if(!event.isCancelled() && event.isCommand()) {
            GuardPlayer<ProxiedPlayer> guardPlayer = main.getPlayerManager().get((ProxiedPlayer) event.getSender());
            if (guardPlayer != null) {
                String enter = main.getCommandManager().getEnterString(event.getMessage());
                if (guardPlayer.getToken() != null) {
                    if (!guardPlayer.hasAccess() && !enter.equalsIgnoreCase("guard")) {
                        event.setCancelled(true);
                        guardPlayer.getPlayer().sendMessage(TextComponent.fromLegacyText(main.getConf().get("chat.protect.notAccess")));
                        main.getNotifyChannel().send(Notification.COMMAND_NOT_ACCESS, guardPlayer.getPlayer().getName());
                    }
                    return;
                } else if (!verify(guardPlayer)) {
                    event.setCancelled(true);
                    guardPlayer.getPlayer().disconnect(TextComponent.fromLegacyText(main.getConf().get("chat.protect.permission")));
                    guardPlayer.getPlayer().sendMessage(TextComponent.fromLegacyText(main.getConf().get("chat.hide")));
                    main.getNotifyChannel().send(Notification.PERMISSION, guardPlayer.getPlayer().getName());
                    return;
                } if(!guardPlayer.getPlayer().hasPermission("guard.show") || !guardPlayer.hasAccess()) {
                    if (main.getCommandManager().isHideCommand(enter)) {
                        event.setCancelled(true);

                        guardPlayer.getPlayer().sendMessage(TextComponent.fromLegacyText(main.getConf().get("chat.hide")));
                        main.getNotifyChannel().send(Notification.HIDE_COMMAND, guardPlayer.getPlayer().getName(), enter);
                    }
                }
            } else event.getSender().disconnect(TextComponent.fromLegacyText(main.getConf().get("chat.protect.incorrectSession")));
        }
    }


    public boolean verify(GuardPlayer<ProxiedPlayer> guardPlayer) {
        if(guardPlayer.getToken() == null) {
            for (String permission : main.getConf().getResource().getStringList("protect.permission"))
                if (guardPlayer.getPlayer().hasPermission(permission)) return false;
            return true;//guardPlayer.getPlayer().getGroups().size() == 0;
        }
        return true;
    }







}
