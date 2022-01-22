package fr.asvadia.guard.bukkit.object;

import fr.asvadia.api.common.util.DiscordWebhook;
import fr.asvadia.api.common.util.PlaceHolder;
import fr.asvadia.guard.bukkit.BukkitMain;
import fr.asvadia.guard.common.player.GuardPlayer;
import fr.asvadia.guard.util.Notification;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.*;

public class NotifyManager {

    BukkitMain main;

    public NotifyManager(BukkitMain main) {
        this.main = main;
    }

    public void message(Notification.Level level, PlaceHolder... placeHolders) {
        Bukkit.getOnlinePlayers().forEach(p -> {
            GuardPlayer<Player> player = main.getPlayerManager().get(p);
            if (player.hasAccess() && p.hasPermission("guard.notify"))
                p.sendMessage(PlaceHolder.replace(main.getConf().get("chat.notify." + level.getKey() + ".message"), placeHolders));
        });
    }


    public void log(Notification.Level level, PlaceHolder... placeHolders) {
       System.out.println(PlaceHolder.replace(main.getConf().get("chat.notify." + level.getKey() + ".log"), placeHolders));
    }

    public void discord(Notification.Level level, PlaceHolder... placeHolders) {
        Bukkit.getScheduler().runTask(main, () -> {
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
        if (main.hasProxy() && main.getServer().getOnlinePlayers().size() > 0) {
            List<String> list = new ArrayList<>(Arrays.asList("notify", notification.toString(), name));
            list.addAll(Arrays.asList(args));

            main.getRequestManager().request(new ArrayList<>(Bukkit.getOnlinePlayers()).get(new Random().nextInt(Bukkit.getOnlinePlayers().size())), list.toArray(new String[list.size()]));
        } else {
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

}
