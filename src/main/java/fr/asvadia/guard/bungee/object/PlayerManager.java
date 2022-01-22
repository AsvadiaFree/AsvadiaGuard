package fr.asvadia.guard.bungee.object;

import fr.asvadia.api.common.security.Totp;
import fr.asvadia.guard.bukkit.BukkitMain;
import fr.asvadia.guard.bungee.ProxyMain;
import fr.asvadia.guard.common.player.GuardPlayer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.bukkit.entity.Player;

import java.util.HashMap;

public class PlayerManager {

    ProxyMain main;

    HashMap<ProxiedPlayer, GuardPlayer<ProxiedPlayer>> guardPlayers;

    public PlayerManager(ProxyMain main) {
        guardPlayers = new HashMap<>();
        this.main = main;
    }

    public GuardPlayer<ProxiedPlayer> get(ProxiedPlayer player) {
        return guardPlayers.get(player);
    }

    public HashMap<ProxiedPlayer, GuardPlayer<ProxiedPlayer>> getMap() {
        return guardPlayers;
    }

    public void load(ProxiedPlayer player) {
        GuardPlayer<ProxiedPlayer> guardPlayer;
        String path = "protect." + player.getUniqueId() + ".token";
        if(main.getConf().getResource().getStringList("protect.allow").contains(player.getName())) {
            if(main.getData().getResource().contains(path)) {
                guardPlayer = new GuardPlayer<>(player, main.getData().get(path));
            } else {
                String token = Totp.generateSecretKey();
                main.getData().getResource().set(path, token);
                guardPlayer = new GuardPlayer<>(player, token);
            }
        } else {
            if(main.getData().getResource().contains(path)) main.getData().getResource().set("protect." + player.getUniqueId(), null);
            guardPlayer = new GuardPlayer<>(player);
        }
        guardPlayers.put(player, guardPlayer);
    }



    public void unLoad(GuardPlayer<Player> player) {
        if(player.getToken() != null) {
            String path = "protect." + player.getPlayer().getUniqueId() + ".token";
            if(!main.getData().getResource().getString("protect." + player.getPlayer().getUniqueId() + ".token").equals(player.getToken()))
                main.getData().getResource().set(path, player.getToken());
        }
    }

}
