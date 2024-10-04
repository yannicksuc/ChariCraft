package fr.lordfinn.charicraft;

import com.google.gson.*;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Timer;
import java.util.TimerTask;

public class StreamlabsWebSocketClient extends WebSocketClient {

    private final Streamer streamer;
    private Timer pingTimer; // Timer to send pings

    public StreamlabsWebSocketClient(String uri, Streamer streamer) throws URISyntaxException {
        super(new URI(uri));
        this.streamer = streamer;
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        System.out.println("Connected to Streamlabs Charity WebSocket");
        startPingTimer();
    }

    @Override
    public void onMessage(String message) {
        String previewMessage = message.length() > 50 ? message.substring(0, 50) + "..." : message;
        handleMessage(message);
    }

    private void handleMessage(String message) {
        try {
            String jsonString = extractJsonString(message);
            JsonElement jsonElement = JsonParser.parseString(jsonString);

            if (jsonElement.isJsonArray()) {
                processJsonArray(jsonElement.getAsJsonArray());
            }
        } catch (JsonSyntaxException | IllegalStateException e) {
            logParsingError(message, e);
        }
    }

    private String extractJsonString(String message) {
        int jsonStartIndex = message.indexOf("[");
        if (jsonStartIndex == -1) {
            jsonStartIndex = message.indexOf("{");
        }
        if (jsonStartIndex != -1) {
            return message.substring(jsonStartIndex);
        }
        return message;
    }

    private void processJsonArray(JsonArray jsonArray) {
        if (isValidEventArray(jsonArray)) {
            JsonObject eventData = jsonArray.get(1).getAsJsonObject();
            String eventType = eventData.get("type").getAsString();

            if ("twitchcharitydonation".equals(eventType)) {
                processCharityDonations(eventData);
            }
        } else {
            handleConnectionData(jsonArray);
        }
    }

    private boolean isValidEventArray(JsonArray jsonArray) {
        return jsonArray.size() >= 2 && "event".equals(jsonArray.get(0).getAsString());
    }

    private void handleConnectionData(JsonArray jsonArray) {
        if (jsonArray.size() > 1) {
            JsonObject connectionData = jsonArray.get(1).getAsJsonObject();
            int pingInterval = connectionData.get("pingInterval").getAsInt();
            startPingTimer(pingInterval);
        }
    }

    private void startPingTimer() {
        startPingTimer(10000); // Default ping interval is 10 seconds
    }

    private void startPingTimer(int pingInterval) {
        stopPingTimer(); // Stop any existing timer
        pingTimer = new Timer();
        pingTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                sendPing();
            }
        }, pingInterval, pingInterval);
    }

    public void sendPing() {
        // Sending a ping message to keep the connection alive
        send("2"); // This is a typical socket.io ping message
    }

    private void processCharityDonations(JsonObject eventData) {
        CharityDonationEvent donationEvent = new Gson().fromJson(eventData, CharityDonationEvent.class);
        for (CharityDonationEvent.DonationMessage message : donationEvent.getMessage()) {
            streamer.handleDonation(message);
        }
    }

    private void logParsingError(String message, Exception e) {
        ChariCraft.getInstance().getLogger().warning("Failed to parse message: " + message + ". Error: " + e.getMessage());
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("WebSocket closed with exit code " + code + " additional info: " + reason);
        stopPingTimer(); // Stop the ping timer if the connection is closed
    }

    @Override
    public void onError(Exception ex) {
        System.err.println("An error occurred during WebSocket operation: " + ex.getMessage());
        ex.printStackTrace();
    }

    private void stopPingTimer() {
        if (pingTimer != null) {
            pingTimer.cancel();
            pingTimer = null;
        }
    }
}
