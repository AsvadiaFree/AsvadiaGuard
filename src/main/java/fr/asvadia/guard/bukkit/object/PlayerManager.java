package fr.asvadia.guard.bukkit.object;

import fr.asvadia.api.common.security.Totp;
import fr.asvadia.guard.bukkit.BukkitMain;
import fr.asvadia.guard.common.player.GuardPlayer;
import org.bukkit.craftbukkit.libs.it.unimi.dsi.fastutil.Hash;
import org.bukkit.entity.Player;

import java.util.HashMap;

public class PlayerManager {

    BukkitMain main;

    HashMap<Player, GuardPlayer<Player>> guardPlayers;

    public PlayerManager(BukkitMain main) {
        guardPlayers = new HashMap<>();
        this.main = main;
    }

    public GuardPlayer<Player> get(Player player) {
        return guardPlayers.get(player);
    }

    public HashMap<Player, GuardPlayer<Player>> getMap() {
        return guardPlayers;
    }

    public GuardPlayer<Player> load(Player player) {
        GuardPlayer<Player> guardPlayer;
        String path = "protect." + player.getUniqueId() + ".token";
        if(main.getConf().getResource().getStringList("protect.allow").contains(player.getName())) {
            if(main.getData().getResource().isSet(path)) {
                guardPlayer = new GuardPlayer<>(player, main.getData().get(path));
            } else {
                String token = Totp.generateSecretKey();
                main.getData().getResource().set(path, token);
                guardPlayer = new GuardPlayer<>(player, token);
            }
        } else {
            if(main.getData().getResource().isSet(path)) main.getData().getResource().set("protect." + player.getUniqueId(), null);
            guardPlayer = new GuardPlayer<>(player);
        }
        guardPlayers.put(player, guardPlayer);
        return guardPlayer;
    }

    public void unLoad(GuardPlayer<Player> player) {
        if(player.getToken() != null) {
            String path = "protect." + player.getPlayer().getUniqueId() + ".token";
            if(!main.getData().getResource().getString("protect." + player.getPlayer().getUniqueId() + ".token").equals(player.getToken()))
                main.getData().getResource().set(path, player.getToken());
        }
    }

}
