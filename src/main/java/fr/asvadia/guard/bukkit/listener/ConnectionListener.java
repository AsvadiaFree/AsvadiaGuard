package fr.asvadia.guard.bukkit.listener;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.injector.server.TemporaryPlayerFactory;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import fr.asvadia.guard.bukkit.BukkitMain;
import fr.asvadia.guard.util.Notification;
import fr.asvadia.guard.bukkit.util.HandShakeVerify;

import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;


public class ConnectionListener extends PacketAdapter implements Listener {


    Set<UUID> handShakeVerified;
    BukkitMain main;

    public ConnectionListener(BukkitMain main) {
        super(main, ListenerPriority.LOWEST, PacketType.Handshake.Client.SET_PROTOCOL);
        handShakeVerified = new HashSet<>();
        this.main = main;
    }


    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
        if(handShakeVerified.contains(event.getPlayer().getUniqueId())) {
            handShakeVerified.remove(event.getPlayer().getUniqueId());
            main.getPlayerManager().load(event.getPlayer());
        } else event.disallow(PlayerLoginEvent.Result.KICK_WHITELIST, main.getConf().get("chat.connection.invalidPacket"));
    }

    @EventHandler
    public void onDisconnectPlayer(PlayerQuitEvent event) {
        main.getPlayerManager().unLoad(main.getPlayerManager().get(event.getPlayer()));
    }

    @Override
    public void onPacketReceiving(PacketEvent event) {
        if(main.hasProxy()) {
            PacketContainer packet = event.getPacket();

            if (packet.getProtocols().read(0) != PacketType.Protocol.LOGIN) return;

            String handshake = packet.getStrings().read(0);
            HandShakeVerify decoded = new HandShakeVerify();
            decoded.decodeAndVerify(handshake, main.getToken());
            if (decoded.getStatus() != HandShakeVerify.Status.SUCCESS) {

                String kickMessage;
                if (decoded.getStatus() == HandShakeVerify.Status.INCORRECT_TOKEN) kickMessage = main.getConf().get("chat.connection.incorrectToken");
                else if (decoded.getStatus() == HandShakeVerify.Status.NO_TOKEN) kickMessage = main.getConf().get("chat.connection.notToken");
                else kickMessage = main.getConf().get("chat.connection.invalidPacket");

                try {
                    closeConnection(event.getPlayer(), kickMessage);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                packet.getStrings().write(0, "null");
                main.getNotifyChannel().send(Notification.INCORRECT_CONNECTION, decoded.getConnectionDescription());
            } else {
                packet.getStrings().write(0, decoded.encode());
                handShakeVerified.add(decoded.getUniqueId());
            }
        }
    }

    private void closeConnection(Player player, String kickMessage) throws Exception {
        if(player != null && kickMessage != null) {
            PacketContainer packet = new PacketContainer(PacketType.Login.Server.DISCONNECT);
            packet.getModifier().writeDefaults();

            WrappedChatComponent component = WrappedChatComponent.fromJson(ComponentSerializer.toString(TextComponent.fromLegacyText(kickMessage)));
            packet.getChatComponents().write(0, component);

            ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet);
            TemporaryPlayerFactory.getInjectorFromPlayer(player).getSocket().close();
        }
    }

}
