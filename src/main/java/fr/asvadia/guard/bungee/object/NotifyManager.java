package fr.asvadia.guard.bungee.object;

import fr.asvadia.api.common.util.DiscordWebhook;
import fr.asvadia.api.common.util.PlaceHolder;
import fr.asvadia.guard.bungee.ProxyMain;
import fr.asvadia.guard.common.player.GuardPlayer;
import fr.asvadia.guard.util.Notification;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.io.IOException;

public class NotifyManager {

    ProxyMain main;

    public NotifyManager(ProxyMain main) {
        this.main = main;
    }

    public void message(Notification.Level level, PlaceHolder... placeHolders) {
        ProxyServer.getInstance().getPlayers().forEach(p -> {
            GuardPlayer<ProxiedPlayer> player = main.getPlayerManager().get(p);
            if (player.hasAccess() && p.hasPermission("guard.notify"))
                p.sendMessage(TextComponent.fromLegacyText(PlaceHolder.replace(main.getConf().get("chat.notify." + level.getKey() + ".message"), placeHolders)));
        });
    }


    public void log(Notification.Level level, PlaceHolder... placeHolders) {
       System.out.println(PlaceHolder.replace(main.getConf().get("chat.notify." + level.getKey() + ".log"), placeHolders));
    }

    public void discord(Notification.Level level, PlaceHolder... placeHolders) {
        ProxyServer.getInstance().getScheduler().runAsync(main, () -> {
            if (main.getWebhook() != null) {
                DiscordWebhook webhook = new DiscordWebhook(main.getWebhook());
                webhook.setContent(PlaceHolder.replace(main.getConf().get("chat.notify." + level.getKey() + ".discord"), placeHolders));
                try {
                    webhook.execute();
                } catch (IOException ignored) {}
            }
        });

    }


    public void send(Notification notification, String name, String... args) {
        PlaceHolder pType = new PlaceHolder("type", notification.toString());
        PlaceHolder pName = new PlaceHolder("name", name);
        PlaceHolder pArgs = new PlaceHolder("args", "");

        if (args.length > 0) {
            StringBuilder builder = new StringBuilder(main.getConf().get("chat.notify.args.start")).append(args[0]);
            for (int i = 1; i < args.length; i++)
                builder.append(builder.append((String) main.getConf().get("chat.notify.args.separator")).append(args[i]));
            builder.append((String) main.getConf().get("chat.notify.args.end"));
            pArgs.setValue(builder.toString());
        }

        PlaceHolder[] placeHolders = {pType, pName, pArgs};
        log(notification.getLevel(), placeHolders);
        message(notification.getLevel(), placeHolders);
        discord(notification.getLevel(), placeHolders);
    }
}
