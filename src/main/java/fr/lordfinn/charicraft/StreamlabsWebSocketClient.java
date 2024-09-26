package fr.lordfinn.charicraft;

import com.google.gson.Gson;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;

public class StreamlabsWebSocketClient extends WebSocketClient {

    public StreamlabsWebSocketClient(String uri) throws URISyntaxException {
        super(new URI(uri));
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        System.out.println("Connected to Streamlabs Charity WebSocket");
    }

    @Override
    public void onMessage(String message) {
        System.out.println("Received donation event: " + message);

        // Parse le message JSON reçu depuis la WebSocket
        // Exemple basique pour extraire les informations de donation
        Gson gson = new Gson();
        DonationEvent event = gson.fromJson(message, DonationEvent.class);

        // Appelle la méthode pour gérer les dons dans le plugin
        Charicraft.getInstance().onDonationReceived(event.getStreamerName(), event.getAmount());
    }


    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("WebSocket closed with exit code " + code + " additional info: " + reason);
    }

    @Override
    public void onError(Exception ex) {
        System.err.println("An error occurred:" + ex.getMessage());
        ex.printStackTrace();
    }
}
