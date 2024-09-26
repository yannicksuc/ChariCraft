package fr.lordfinn.charicraft;

import fr.lordfinn.charicraft.commands.FixDonationAmountCommand;
import fr.lordfinn.charicraft.commands.TriggerFakeDonationCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.List;
import java.util.Objects;

public final class ChariCraft extends JavaPlugin {

    private static ChariCraft instance;
    private WebSocketManager webSocketManager;
    private List<Streamer> streamers;

    @Override
    public void onEnable() {
        instance = this;

        // Load streamer configuration
        loadStreamersConfig();

        // Initialize WebSocket manager and connect to streamers
        webSocketManager = new WebSocketManager();
        webSocketManager.connectToStreamers(streamers);

        Objects.requireNonNull(this.getCommand("FixDonationAmount")).setExecutor(new FixDonationAmountCommand());
        Objects.requireNonNull(this.getCommand("triggerFakeDonation")).setExecutor(new TriggerFakeDonationCommand(streamers));
        getLogger().info("Charicraft plugin enabled.");
    }

    private void loadStreamersConfig() {
        StreamersConfigLoader configLoader = new StreamersConfigLoader();
        File configFile = new File(getDataFolder(), "streamers.json");

        if (!configFile.exists()) {
            getLogger().severe("Streamers configuration file not found at: " + configFile.getAbsolutePath());
            return;
        }

        streamers = configLoader.loadStreamers(configFile.getAbsolutePath());

        if (streamers != null && !streamers.isEmpty()) {
            getLogger().info("Loaded streamers configuration.");
        } else {
            getLogger().warning("No streamers found or failed to load the configuration.");
        }
    }

    @Override
    public void onDisable() {
        // Properly close all WebSocket connections
        if (webSocketManager != null) {
            webSocketManager.closeAllConnections();
        }

        getLogger().info("Charicraft plugin disabled.");
    }

    public static ChariCraft getInstance() {
        return instance;
    }
}
