package fr.lordfinn.charicraft.actions;

import fr.lordfinn.charicraft.ChariCraft;
import fr.lordfinn.charicraft.CharityDonationEvent;
import fr.lordfinn.charicraft.Streamer;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Parrot;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.Duration;
import java.util.Objects;
import java.util.Random;

public class GaySheepHealthBoostAction extends AbstractDonationAction {

    private Sheep sheep;
    private Player player;

    public GaySheepHealthBoostAction() {
        super(Duration.ofMinutes(2).plusSeconds(18), "Bip Bip I'm a ", "Fais apparaite un mouton multicolor aux pouvoir magiques", BossBar.Color.BLUE);
    }

    @Override
    public void onStart(Streamer streamer, CharityDonationEvent.DonationMessage message, Audience audience) {
        ChariCraft.getInstance().getLogger().info("NAME " + streamer.getMinecraftUsername());

        World world = streamer.getBukkitPlayer().getWorld();
        Location loc = streamer.getBukkitPlayer().getLocation();

        // Summon sheep
        sheep = (Sheep) world.spawnEntity(loc, EntityType.SHEEP);
        Objects.requireNonNull(sheep.getAttribute(Attribute.GENERIC_SCALE)).setBaseValue(1);
        sheep.customName(Component.text("_jeb"));
        sheep.setCustomNameVisible(false);
        sheep.setInvulnerable(true);

        // Summon parrot as passenger
        Parrot parrot = (Parrot) world.spawnEntity(loc, EntityType.PARROT);
        parrot.setInvulnerable(true);

        sheep.addPassenger(parrot);

        // Play song
        audience.playSound(Sound.sound(Key.key("musics.beep"), Sound.Source.PLAYER, 1.0f, 1.0f));
        final double[] scale = {0.5};

        new BukkitRunnable() {
            private final Random random = new Random();
            private int tick = 0;

            @Override
            public void run() {
                if (tick % 10 == 0) { // Change every half second (126 BPM approx. pacing)
                    parrot.setVariant(Parrot.Variant.values()[random.nextInt(Parrot.Variant.values().length)]);
                    sheep.setColor(DyeColor.values()[random.nextInt(DyeColor.values().length)]);
                    scale[0] = (scale[0] == 2.0 ? 0.5 : 2.0);
                    Objects.requireNonNull(sheep.getAttribute(Attribute.GENERIC_SCALE)).setBaseValue(scale[0]);
                    Objects.requireNonNull(parrot.getAttribute(Attribute.GENERIC_SCALE)).setBaseValue((scale[0] == 2.0 ? 0.8 : 1.2));
                }

                // Apply potion effects
                world.getNearbyEntities(sheep.getLocation(), 3, 3, 3).stream()
                        .filter(e -> e instanceof Player)
                        .map(e -> (Player) e)
                        .forEach(p -> {
                            p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 60, 5, true, false));
                        });

                tick++;
            }
        }.runTaskTimer(ChariCraft.getInstance(), 0L, 1L); // Run every tick
    }

    @Override
    public void onEnd(Streamer streamer, CharityDonationEvent.DonationMessage message, Audience audience) {
        if (sheep != null && !sheep.isDead()) {
            Location loc = sheep.getLocation();
            sheep.getPassengers().getFirst().remove();
            sheep.remove();
            streamer.getBukkitPlayer().getWorld().spawnParticle(Particle.FALLING_DUST, loc, 100, 1.0, 1.0, 1.0, 1, new Particle.DustOptions(Color.fromRGB(255, 0, 255), 1));
        }
    }
}