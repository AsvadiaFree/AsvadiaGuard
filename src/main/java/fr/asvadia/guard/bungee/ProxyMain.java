package fr.asvadia.guard.bungee;

import fr.asvadia.api.bungee.AsvadiaAPI;
import fr.asvadia.api.bungee.config.YMLConfig;
import fr.asvadia.api.bungee.messaging.RequestManager;
import fr.asvadia.api.common.security.Totp;
import fr.asvadia.guard.bungee.command.GuardCommand;
import fr.asvadia.guard.bungee.listener.ConnectionListener;
import fr.asvadia.guard.bungee.listener.ProtectListener;
import fr.asvadia.guard.bungee.listener.ServerListener;
import fr.asvadia.guard.bungee.object.CommandManager;
import fr.asvadia.guard.bungee.object.NotifyManager;
import fr.asvadia.guard.bungee.object.PlayerManager;
import net.md_5.bungee.api.plugin.Plugin;

import java.util.concurrent.TimeUnit;

public class ProxyMain extends Plugin {

    YMLConfig config;

    YMLConfig data;

    String token;

    NotifyManager notifyChannel;

    RequestManager requestManager;

    CommandManager commandManager;

    PlayerManager playerManager;

    @Override
    public void onEnable() {
        config = new YMLConfig(getDataFolder().toString(), "config", this);
        data = new YMLConfig(getDataFolder().toString(), "data", this);

        String token = config.get("proxy.token");
        if (token == null || token.isEmpty()) {
            token = Totp.generateSecretKey();
            config.getResource().set("proxy.token", token);
            config.save();
        }
        this.token = token;

        playerManager = new PlayerManager(this);
        commandManager = new CommandManager(this);
        requestManager = new RequestManager("azirixx:guard", getToken());
        AsvadiaAPI.getInstance().registerRequestManager(requestManager);

        getProxy().getPluginManager().registerListener(this, new ServerListener(this));

        getProxy().getPluginManager().registerListener(this, new ProtectListener(this));

        getProxy().getPluginManager().registerListener(this, new ConnectionListener(this));

        getProxy().getPluginManager().registerCommand(this, new GuardCommand(this, "guard"));

        notifyChannel = new NotifyManager(this);

        getProxy().getPlayers().forEach(playerManager::load);
        getProxy().getScheduler().schedule(this, commandManager::load, 250, TimeUnit.MILLISECONDS);
    }

    @Override
    public void onDisable() {
        data.save();
        getProxy().stop();
    }

    public YMLConfig getConf() {
        return config;
    }

    public YMLConfig getData() {
        return data;
    }


    public NotifyManager getNotifyChannel() {
        return notifyChannel;
    }

    public RequestManager getRequestManager() {
        return requestManager;
    }

    public PlayerManager getPlayerManager() {
        return playerManager;
    }

    public CommandManager getCommandManager() {
        return commandManager;
    }

    public boolean hasProxy() {
        return getConf().get("proxy.enable");
    }

    public String getToken() {
        return token;
    }

    public String getWebhook() {
        return config.get("webhook");
    }

    public boolean hasSynchronisation() {
        return config.get("protect.synchronisation");
    }


}
