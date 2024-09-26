package fr.lordfinn.charicraft;

import java.util.ArrayList;
import java.util.List;

public class WebSocketManager {
    private final List<StreamlabsWebSocketClient> webSocketClients = new ArrayList<>();
    private static final String STREAMLABS_SOCKET_URI = "wss://sockets.streamlabs.com/socket.io/?transport=websocket&token=";

    public void connectToStreamers(List<Streamer> streamers) {
        for (Streamer streamer : streamers) {
            connectToStreamerWithRetries(streamer, 3);
        }
    }

    private void connectToStreamerWithRetries(Streamer streamer, int maxAttempts) {
        int attempts = 0;
        boolean connected = false;

        while (attempts < maxAttempts && !connected) {
            try {
                connectToStreamer(streamer);
                connected = true;
            } catch (Exception e) {
                attempts++;
                handleConnectionFailure(streamer, attempts);
                waitBeforeRetry();
            }
        }
    }

    private void connectToStreamer(Streamer streamer) throws Exception {
        StreamlabsWebSocketClient webSocketClient = new StreamlabsWebSocketClient(STREAMLABS_SOCKET_URI + streamer.getAccessToken(), streamer);
        webSocketClient.connect();
        webSocketClients.add(webSocketClient);
        ChariCraft.getInstance().getLogger().info("Successfully connected to " + streamer.getMinecraftUsername());
    }

    private void handleConnectionFailure(Streamer streamer, int attempts) {
        ChariCraft.getInstance().getLogger().severe("Connection attempt " + attempts + " failed for " + streamer.getMinecraftUsername() + ". Retrying...");
    }

    private void waitBeforeRetry() {
        try {
            Thread.sleep(2000); // Wait before retrying
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }

    public void closeAllConnections() {
        for (StreamlabsWebSocketClient client : webSocketClients) {
            if (client != null) {
                client.close();
            }
        }
    }
}
