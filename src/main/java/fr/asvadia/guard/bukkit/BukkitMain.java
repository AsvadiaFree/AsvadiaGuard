package fr.asvadia.guard.bukkit;

import com.comphenix.protocol.ProtocolLibrary;
import fr.asvadia.api.bukkit.AsvadiaAPI;
import fr.asvadia.api.bukkit.config.YMLConfig;
import fr.asvadia.api.bukkit.messaging.RequestManager;
import fr.asvadia.api.common.security.Totp;
import fr.asvadia.guard.bukkit.command.GuardCommand;
import fr.asvadia.guard.bukkit.listener.ConnectionListener;
import fr.asvadia.guard.bukkit.object.NotifyManager;
import fr.asvadia.guard.bukkit.listener.ProtectListener;
import fr.asvadia.guard.bukkit.listener.ServerListener;
import fr.asvadia.guard.bukkit.object.CommandManager;
import fr.asvadia.guard.bukkit.object.PlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;


public class BukkitMain extends JavaPlugin {

    YMLConfig config;

    YMLConfig data;

    String token;

    RequestManager requestManager;

    NotifyManager notifyChannel;

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

        getServer().getPluginManager().registerEvents(new ServerListener(this), this);

        ProtectListener protectListener = new ProtectListener(this);
        getServer().getPluginManager().registerEvents(protectListener, this);
        ProtocolLibrary.getProtocolManager().addPacketListener(protectListener);

        ConnectionListener connectionListener = new ConnectionListener(this);
        getServer().getPluginManager().registerEvents(connectionListener, this);
        ProtocolLibrary.getProtocolManager().addPacketListener(connectionListener);

        getCommand("guard").setExecutor(new GuardCommand(this));

        notifyChannel = new NotifyManager(this);

        Bukkit.getOnlinePlayers().forEach(playerManager::load);
        Bukkit.getScheduler().runTaskLater(this, commandManager::load, 5);
    }

    @Override
    public void onDisable() {
        data.save();
        Bukkit.shutdown();
    }

    public YMLConfig getConf() {
        return config;
    }

    public YMLConfig getData() {
        return data;
    }

    public PlayerManager getPlayerManager() {
        return playerManager;
    }

    public CommandManager getHideCommandManager() {
        return commandManager;
    }

    public NotifyManager getNotifyChannel() {
        return notifyChannel;
    }

    public RequestManager getRequestManager() {
        return requestManager;
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