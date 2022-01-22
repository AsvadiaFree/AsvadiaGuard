package fr.asvadia.guard.common.player;

import fr.asvadia.api.common.security.Totp;
import org.bukkit.entity.Player;

public class GuardPlayer <P> {

    P player;
    String token;

    boolean access;

    public GuardPlayer(P player) {
        this(player, null);
    }

    public GuardPlayer(P player, String token) {
        this.player = player;
        this.token = token;
    }

    public P getPlayer() {
        return player;
    }

    public String getToken() {
        return token;
    }

    public boolean hasAccess() {
        return access;
    }

    public boolean access(String code) {
        if(Totp.getTOTPCode(token).equals(code)) {
            this.access = true;
            return true;
        }
        return false;
    }

    public void setToken(String token) {
        this.token = token;
    }
}