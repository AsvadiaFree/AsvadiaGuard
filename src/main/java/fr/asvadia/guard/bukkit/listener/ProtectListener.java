package fr.asvadia.guard.bukkit.listener;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.*;
import fr.asvadia.guard.bukkit.BukkitMain;
import fr.asvadia.guard.common.player.GuardPlayer;
import fr.asvadia.guard.util.Notification;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class ProtectListener extends PacketAdapter implements Listener {

    BukkitMain main;

    public ProtectListener(BukkitMain main) {
        super(main, ListenerPriority.NORMAL, PacketType.Play.Server.TAB_COMPLETE, PacketType.Play.Client.TAB_COMPLETE);
        this.main = main;
    }

    @Override
    public void onPacketSending(PacketEvent event) {
        if(event.getPacketType() == PacketType.Play.Server.TAB_COMPLETE) {
            GuardPlayer<Player> guardPlayer = main.getPlayerManager().get(event.getPlayer());
            if(guardPlayer != null) {
                if (!event.getPlayer().hasPermission("guard.show") || !guardPlayer.hasAccess()) {
                    List<String> suggestions = new ArrayList<>(Arrays.asList(event.getPacket().getStringArrays().read(0)));
                    int size = suggestions.size();

                    suggestions.removeIf(suggestion -> {
                        suggestion = suggestion.replaceFirst("/", "");
                        if (suggestion.contains(":")) suggestion = suggestion.split(":")[1];
                        return main.getHideCommandManager().isHideCommand(suggestion);
                    });
                    if (size != suggestions.size())
                        event.getPacket().getStringArrays().write(0, suggestions.toArray(new String[0]));
                }
            } else event.getPlayer().kickPlayer(main.getConf().get("chat.protect.incorrectSession"));
        }
    }

    @Override
    public void onPacketReceiving(PacketEvent event) {
        if(event.getPacketType() == PacketType.Play.Client.TAB_COMPLETE) {
            GuardPlayer<Player> guardPlayer = main.getPlayerManager().get(event.getPlayer());
            if(guardPlayer != null) {
                if (!event.getPlayer().hasPermission("guard.show") || !guardPlayer.hasAccess()) {
                    String enter = event.getPacket().getStrings().read(0);
                    if (enter.startsWith("/")) {
                        if (main.getHideCommandManager().isHideCommand(main.getHideCommandManager().getEnterString(enter))) event.getPacket().getStrings().write(0, "§");
                    }
                }
            } else event.getPlayer().kickPlayer(main.getConf().get("chat.protect.incorrectSession"));
        }
    }


    @EventHandler
    public void onGameModeCreative(PlayerGameModeChangeEvent event) {
        if(main.getConf().get("protect.creative")) {
            if (event.getNewGameMode() == GameMode.CREATIVE) {
                GuardPlayer<Player> guardPlayer =  main.getPlayerManager().get(event.getPlayer());
                if (guardPlayer != null) {
                    if (guardPlayer.getToken() != null) {
                        if (!guardPlayer.hasAccess()) {
                            event.setCancelled(true);
                            event.getPlayer().setGameMode(GameMode.SURVIVAL);
                            guardPlayer.getPlayer().sendMessage((String) main.getConf().get("chat.protect.notAccess"));
                            main.getNotifyChannel().send(Notification.CREATIVE_NOT_ACCESS, event.getPlayer().getName());
                        }
                    } else {
                        event.setCancelled(true);
                        event.getPlayer().setGameMode(GameMode.SURVIVAL);

                        Bukkit.getBanList(BanList.Type.NAME).addBan(event.getPlayer().getName(), main.getConf().get("chat.protect.gamemode"), null, null);
                        event.getPlayer().kickPlayer(main.getConf().get("chat.protect.gamemode"));

                        main.getNotifyChannel().send(Notification.ILLEGAL_CREATIVE, event.getPlayer().getName());
                    }
                } else event.getPlayer().kickPlayer(main.getConf().get("chat.protect.incorrectSession"));
            }
        }
    }


    @EventHandler(priority = EventPriority.MONITOR)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        if (!event.isCancelled()) {
            GuardPlayer<Player> guardPlayer = main.getPlayerManager().get(event.getPlayer());
            if (guardPlayer != null) {
                String enter = main.getHideCommandManager().getEnterString(event.getMessage());
                if (guardPlayer.getToken() != null) {
                    if (!guardPlayer.hasAccess() && !enter.equalsIgnoreCase("guard")) {
                        event.setCancelled(true);
                        guardPlayer.getPlayer().sendMessage((String) main.getConf().get("chat.protect.notVerified"));
                        main.getNotifyChannel().send(Notification.COMMAND_NOT_ACCESS, event.getPlayer().getName());
                    }
                    return;
                } else if (!verify(guardPlayer)) {
                    event.setCancelled(true);
                    if(event.getPlayer().isOp()) event.getPlayer().setOp(false);
                    Bukkit.getBanList(BanList.Type.NAME).addBan(event.getPlayer().getName(), main.getConf().get("chat.protect.permission"), null, null);
                    event.getPlayer().kickPlayer(main.getConf().get("chat.protect.permission"));

                    guardPlayer.getPlayer().sendMessage((String) main.getConf().get("chat.hide"));
                    main.getNotifyChannel().send(Notification.PERMISSION, event.getPlayer().getName());
                    return;
                } if(!event.getPlayer().hasPermission("guard.show") || !guardPlayer.hasAccess()) {
                    if (main.getHideCommandManager().isHideCommand(enter)) {
                        event.setCancelled(true);

                        guardPlayer.getPlayer().sendMessage((String) main.getConf().get("chat.hide"));
                        main.getNotifyChannel().send(Notification.HIDE_COMMAND, event.getPlayer().getName(), enter);
                    }
                }
            } else event.getPlayer().kickPlayer("§cIncorrect session, please reconnect");
        }
    }

    public boolean verify(GuardPlayer<Player> guardPlayer) {
        if(guardPlayer.getToken() == null) {
            for (String permission : main.getConf().getResource().getStringList("protect.permission"))
                if (guardPlayer.getPlayer().hasPermission(permission)) return false;
            return !guardPlayer.getPlayer().isOp();
        }
        return true;
    }










}
