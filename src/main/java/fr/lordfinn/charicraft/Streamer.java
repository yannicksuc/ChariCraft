package fr.lordfinn.charicraft;

import fr.lordfinn.charicraft.actions.InterfaceDonationAction;
import net.kyori.adventure.identity.Identity;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

public class Streamer {
    private final String minecraftUsername;
    private final String twitchUsername;
    private final String streamlabsAccessToken; // Access token for authentication
    private final Map<Double, String> donationThresholds;
    private Player bukkitPlayer; // Ajout du joueur Bukkit

    public Streamer(String minecraftUsername, String twitchUsername, String streamlabsAccessToken, Map<Double, String> donationThresholds) {
        this.minecraftUsername = minecraftUsername;
        this.twitchUsername = twitchUsername;
        this.streamlabsAccessToken = streamlabsAccessToken;
        this.donationThresholds = donationThresholds;
    }

    public String getMinecraftUsername() {
        return minecraftUsername;
    }

    public String getTwitchUsername() {
        return twitchUsername;
    }

    public String getAccessToken() { // Renamed for clarity
        return streamlabsAccessToken;
    }

    // Getter et setter pour bukkitPlayer
    public Player getBukkitPlayer() {
        if (bukkitPlayer == null) {
            ChariCraft.getInstance().getServer().forEachAudience(member -> {
                if (member instanceof Player && ((Player) member).getName().equals(minecraftUsername)) {
                    setBukkitPlayer((Player) member);
                }
            });
        }
        return bukkitPlayer;
    }

    public void setBukkitPlayer(Player bukkitPlayer) {
        this.bukkitPlayer = bukkitPlayer;
    }

    public void handleDonation(CharityDonationEvent.DonationMessage message) {
        ChariCraft.getInstance().getLogger().warning("TEST Donation : " + message.getAmount());
        for (Map.Entry<Double, String> entry : donationThresholds.entrySet()) {
            ChariCraft.getInstance().getLogger().warning("TEST Donation : " + message.getAmount() + " = ? " + entry.getKey());
            if (message.getAmount() == entry.getKey()) {
                ChariCraft.getInstance().getLogger().warning("OUI");
                triggerAction(entry.getValue() + "Action", message);
            }
        }
    }

    private void triggerAction(String actionClassName, CharityDonationEvent.DonationMessage message) {
        try {
            Class<?> actionClass = Class.forName("fr.lordfinn.charicraft.actions." + actionClassName);
            InterfaceDonationAction action = (InterfaceDonationAction) actionClass.getDeclaredConstructor().newInstance();
            Bukkit.getScheduler().runTask(ChariCraft.getInstance(), () -> {
                action.execute(this, message, Bukkit.getServer().filterAudience(audience1 -> audience1.get(Identity.UUID).isPresent()));
            });
            ChariCraft.getInstance().getLogger().info("Triggered action: " + actionClassName + " for " + minecraftUsername);
        } catch (ClassNotFoundException e) {
            ChariCraft.getInstance().getLogger().severe("Action class not found: " + actionClassName);
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            ChariCraft.getInstance().getLogger().severe("Failed to instantiate action: " + actionClassName);
            e.printStackTrace();
        }
    }
}
