package fr.asvadia.guard.bungee.listener;

import com.google.common.hash.Hashing;
import fr.asvadia.guard.bungee.ProxyMain;
import fr.asvadia.guard.bungee.util.HandShakeEditor;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.connection.InitialHandler;
import net.md_5.bungee.event.EventHandler;

import java.nio.charset.StandardCharsets;

public class ConnectionListener implements Listener {

    ProxyMain main;

    public ConnectionListener(ProxyMain main) {
        this.main = main;
    }

    @EventHandler
    public void onLogin(LoginEvent event) {
        if(main.hasProxy())
            HandShakeEditor.inject((InitialHandler) event.getConnection(), Hashing.sha256().hashString(main.getToken(), StandardCharsets.UTF_8).toString());

    }

    @EventHandler
    public void onPostLogin(PostLoginEvent event) {
        main.getPlayerManager().load(event.getPlayer());
    }


}
