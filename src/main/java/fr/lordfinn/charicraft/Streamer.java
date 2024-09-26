package fr.lordfinn.charicraft;

import fr.lordfinn.charicraft.actions.DonationAction;
import org.bukkit.Bukkit;

import java.util.Map;

public class Streamer {
    private final String minecraftUsername;
    private final String twitchUsername;
    private final String streamlabsCharityAuthKey;
    private final Map<Double, String> donationThresholds; // Paliers de dons et récompenses

    public Streamer(String minecraftUsername, String twitchUsername, String streamlabsCharityAuthKey, Map<Double, String> donationThresholds) {
        this.minecraftUsername = minecraftUsername;
        this.twitchUsername = twitchUsername;
        this.streamlabsCharityAuthKey = streamlabsCharityAuthKey;
        this.donationThresholds = donationThresholds;
    }

    public String getMinecraftUsername() {
        return minecraftUsername;
    }

    public void handleDonation(double amount) {
        for (Map.Entry<Double, String> entry : donationThresholds.entrySet()) {
            if (amount >= entry.getKey()) {
                triggerAction(entry.getValue());
            }
        }
    }

    private void triggerAction(String actionClassName) {
        try {
            Class<?> actionClass = Class.forName("fr.lordfinn.charicraft.actions." + actionClassName);
            DonationAction action = (DonationAction) actionClass.getDeclaredConstructor().newInstance();
            action.execute(minecraftUsername);
        } catch (Exception e) {
            Bukkit.getLogger().severe("Failed to trigger action: " + actionClassName);
            e.printStackTrace();
        }
    }
}
