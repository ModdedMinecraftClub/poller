package club.moddedminecraft.polychat.server.handlers.protomessages;

import club.moddedminecraft.polychat.core.messagelibrary.EventHandler;
import club.moddedminecraft.polychat.core.messagelibrary.ServerProtos;
import club.moddedminecraft.polychat.core.networklibrary.ConnectedClient;
import club.moddedminecraft.polychat.server.OnlineServer;
import com.google.protobuf.Any;
import net.dv8tion.jda.api.entities.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

public final class PlayerStatusChangedMessageHandler {
    private final static Logger logger = LoggerFactory.getLogger(ServerStatusMessageHandler.class);
    private final HashMap<String, OnlineServer> onlineServers;
    private final TextChannel generalChannel;

    public PlayerStatusChangedMessageHandler(HashMap<String, OnlineServer> onlineServers, TextChannel generalChannel) {
        this.onlineServers = onlineServers;
        this.generalChannel = generalChannel;
    }

    @EventHandler
    public void handle(ServerProtos.ServerPlayerStatusChangedEvent msg, ConnectedClient author) {
        String serverId = msg.getNewPlayersOnline().getServerId().toUpperCase();
        String unformattedId = serverId.replaceAll("§.", "").replaceAll("[\\[\\]]", "");
        OnlineServer server = onlineServers.get(unformattedId);

        if (server != null) {
            server.setPlayersOnline(msg.getNewPlayersOnline().getPlayersOnline());
            server.setOnlinePlayerNames(msg.getNewPlayersOnline().getPlayerNamesList());
            ServerProtos.ServerPlayerStatusChangedEvent.PlayerStatus playerStatus = msg.getNewPlayerStatus();

            // forward message to other MC servers;
            Any packedMsg = Any.pack(msg);
            for (OnlineServer onlineServer : onlineServers.values()) {
                ConnectedClient client = onlineServer.getClient();
                if (client != author) {
                    client.sendMessage(packedMsg.toByteArray());
                }
            }

            // send message to Discord;
            String discordMessage = "`[" + unformattedId + "] " + msg.getPlayerUsername() + " has " + msg.getNewPlayerStatus().toString().toLowerCase() + " the game`";
            generalChannel.sendMessage(discordMessage).queue();
        } else {
            logger.error("Server with id \""
                    + unformattedId
                    + "\" has unexpectedly sent ServerPlayerStatusChangedEvent message despite not being marked as online. Have you sent ServerInfo message on server startup?");
        }
    }
}
