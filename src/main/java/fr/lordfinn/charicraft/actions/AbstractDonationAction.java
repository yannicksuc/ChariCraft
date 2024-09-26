package fr.lordfinn.charicraft.actions;

import fr.lordfinn.charicraft.ChariCraft;
import fr.lordfinn.charicraft.CharityDonationEvent;
import fr.lordfinn.charicraft.Streamer;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.bossbar.BossBar.Overlay;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.Duration;

public abstract class AbstractDonationAction implements InterfaceDonationAction {

    protected Duration duration;
    protected final String name;
    protected final String description;
    protected double value;
    protected final BossBar.Color color;

    // Constructor with an Adventure audience instance
    public AbstractDonationAction(Duration duration, String name, String description, BossBar.Color color) {
        this.duration = duration;
        this.name = name;
        this.description = description;
        this.value = 0;
        this.color = color;
    }

    @Override
    public void execute(Streamer streamer, CharityDonationEvent.DonationMessage message, Audience audience) {

        this.value = message.getAmount();

        // Créer la boss bar
        BossBar bossBar = BossBar.bossBar(
                Component.text(message.getFrom() + " a déclenché " + name + " avec un don de " + message.getAmount() + "€"),
                1.0f,  // Progrès initial (1.0 = plein)
                color,  // Couleur
                Overlay.PROGRESS  // Style
        );

        audience.showBossBar(bossBar);  // Afficher la boss bar
        audience.sendActionBar(Component.text(name + ": " + description, NamedTextColor.GOLD));  // Envoyer l'action bar

        onStart(streamer, message, audience);

        // Gestion du remplissage de la barre et du timer
        new BukkitRunnable() {
            double timeRemaining = duration.toMillis();

            @Override
            public void run() {
                if (timeRemaining <= 0) {
                    bossBar.progress(0);
                    audience.hideBossBar(bossBar);  // Retire la boss bar
                    audience.sendActionBar(Component.empty());  // Enlève l'action bar
                    audience.playSound(Sound.sound(Key.key("block.anvil.land"), Sound.Source.PLAYER, 1.0f, 1.0f));
                    onEnd(streamer, message, audience);  // Appel la méthode supplémentaire
                    cancel();
                    return;
                }

                bossBar.progress((float) (timeRemaining / duration.toMillis()));  // Met à jour la progression
                timeRemaining -= 100;
            }
        }.runTaskTimer(ChariCraft.getInstance(), 0L, 2L);  // Ajuste la fréquence d'update ici (toutes les 0.1 secondes)
    }

    // Hook method to be customized by subclasses (e.g., sound effects or messages on start)
    protected void onStart(Streamer streamer, CharityDonationEvent.DonationMessage message, Audience audience) {
        // Default implementation (can be overridden)
        audience.playSound(Sound.sound(Key.key("entity.player.levelup"), Sound.Source.PLAYER, 1.0f, 1.0f));  // Jouer un son au début
    }

    // Hook method for custom behavior when the action ends
    protected void onEnd(Streamer streamer, CharityDonationEvent.DonationMessage message, Audience audience) {
        // Default behavior is to do nothing (subclasses can override)
    }
}
