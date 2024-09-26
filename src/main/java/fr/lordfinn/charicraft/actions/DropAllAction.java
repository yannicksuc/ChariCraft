package fr.lordfinn.charicraft.actions;

import fr.lordfinn.charicraft.CharityDonationEvent;
import fr.lordfinn.charicraft.Streamer;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.time.Duration;
import java.util.Random;

public class DropAllAction extends AbstractDonationAction {

    public DropAllAction() {
        super(Duration.ofSeconds(5), "Drop tout tout", "On a donné de l'argent, donne ton stuff !", BossBar.Color.YELLOW);
    }

    @Override
    public void onStart(Streamer streamer, CharityDonationEvent.DonationMessage message, Audience audience) {
        audience.sendMessage(Component.text(this.description));
        audience.playSound(Sound.sound(Key.key("entity.player.levelup"), Sound.Source.PLAYER, 1.0f, 1.0f));

        audience.forEachAudience(player ->{
            try {
                dropItems((Player) player);
            } catch (ClassCastException ignored) {}
        });
    }

    private void dropItems(Player player) {
        Random random = new Random();

        // Loop through each item in the player's inventory
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getAmount() > 0) {
                // Remove item from the player's inventory
                player.getInventory().remove(item);

                double xOffset = (random.nextDouble() * 2 - 1); // Valeur entre -radius et +radius
                double yOffset = (random.nextDouble());
                double zOffset = (random.nextDouble() * 2 - 1);
                double force = (1 + random.nextDouble() * 2);

                // Créer une nouvelle position en ajoutant les offsets
                Location dropLocation = player.getLocation().add(xOffset, yOffset, zOffset);

                // Lâcher l'item à la nouvelle position
                var droppedItem = player.getWorld().dropItem(player.getLocation(), item);

                // Appliquer une force pour propulser l'item vers l'extérieur
                Vector direction = droppedItem.getLocation().toVector().subtract(dropLocation.toVector()).normalize();
                droppedItem.setVelocity(direction.multiply(force)); // Ajustez le multiplicateur pour la force désirée
            }
        }
    }
}
