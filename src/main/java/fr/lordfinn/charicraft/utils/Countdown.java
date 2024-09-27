package fr.lordfinn.charicraft.utils;

import fr.lordfinn.charicraft.ChariCraft;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.Duration;

public class Countdown {

    public static void start(Player audience, int from, int to, NamedTextColor color, String subtitle) {
        new BukkitRunnable() {
            int count = from;

            @Override
            public void run() {
                if (count < to) {
                    this.cancel();
                    return;
                }
                Component titleComponent = Component.text(String.valueOf(count)).color(color);
                Component subtitleComponent = Component.text(subtitle).color(color);

                Title title = Title.title(
                        titleComponent,
                        subtitleComponent,
                        Title.Times.times(Duration.ofMillis(500), Duration.ofSeconds(1), Duration.ofMillis(500))
                );
                audience.showTitle(title);
                count--;
            }
        }.runTaskTimer(ChariCraft.getInstance(), 0, 20);
    }

}
