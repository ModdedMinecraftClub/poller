package club.moddedminecraft.polychat.server.handlers.protomessages;

import club.moddedminecraft.polychat.core.messagelibrary.EventHandler;
import club.moddedminecraft.polychat.core.messagelibrary.ServerProtos;
import club.moddedminecraft.polychat.core.networklibrary.ConnectedClient;
import club.moddedminecraft.polychat.server.OnlineServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

public final class PlayersOnlineMessageHandler {
    private final static Logger logger = LoggerFactory.getLogger(PlayersOnlineMessageHandler.class);
    private final HashMap<String, OnlineServer> onlineServers;

    public PlayersOnlineMessageHandler(HashMap<String, OnlineServer> onlineServers) {
        this.onlineServers = onlineServers;
    }

    @EventHandler
    public void handle(ServerProtos.ServerPlayersOnline msg, ConnectedClient author) {
        String serverId = msg.getServerId().toUpperCase();
        String unformattedId = serverId.replaceAll("§.", "").replaceAll("[\\[\\]]", "");
        OnlineServer server = onlineServers.get(unformattedId);

        if (server != null) {
            server.setPlayersOnline(msg.getPlayersOnline());
            server.setOnlinePlayerNames(msg.getPlayerNamesList());
        } else {
            logger.error("Server with id \""
                    + unformattedId
                    + "\" has unexpectedly sent ServerPlayersOnline message despite not being marked as online. Have you sent ServerInfo message on server startup?");
        }
    }
}
