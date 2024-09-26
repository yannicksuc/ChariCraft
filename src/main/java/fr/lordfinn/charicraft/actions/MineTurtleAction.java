package fr.lordfinn.charicraft.actions;

import fr.lordfinn.charicraft.CharityDonationEvent;
import fr.lordfinn.charicraft.Streamer;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.key.Key;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;

import java.time.Duration;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Queue;
import java.util.UUID;

public class MineTurleAction extends AbstractDonationAction {

    Queue<UUID> turtleQueue = new LinkedList<>();

    public MineTurleAction() {
        super(Duration.ofSeconds(3), "Mine Turle", "Hello !", BossBar.Color.GREEN);
    }

    @Override
    public void onStart(Streamer streamer, CharityDonationEvent.DonationMessage message, Audience audience) {
        audience.forEachAudience(player -> {
            Player bukkitPlayer = (Player) player;
            if (bukkitPlayer != null) {

                Turtle turtle = bukkitPlayer.getWorld().spawn(bukkitPlayer.getLocation(), Turtle.class);
                Creeper creeper = (Creeper) bukkitPlayer.getWorld().spawnEntity(bukkitPlayer.getLocation(), EntityType.CREEPER);
                turtle.addPassenger(creeper);
                Objects.requireNonNull(turtle.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED)).setBaseValue(0);
                creeper.setAI(false);
                creeper.setPowered(true);
                Objects.requireNonNull(creeper.getAttribute(Attribute.GENERIC_SCALE)).setBaseValue(0.5);

                // Add turtle to queue
                turtleQueue.add(turtle.getUniqueId());
            }
        });
    }

    @Override
    public void onEnd(Streamer streamer, CharityDonationEvent.DonationMessage message, Audience audience) {
        while (!turtleQueue.isEmpty()) {
            UUID turtleUuid = turtleQueue.poll();
            Turtle turtle = (Turtle) Bukkit.getEntity(turtleUuid);
            if (turtle != null) {
                // Remove passenger (Creeper) first
                if (!turtle.getPassengers().isEmpty()) {
                    turtle.getPassengers().forEach(Entity::remove);
                }
                turtle.getWorld().spawnParticle(Particle.EXPLOSION, turtle.getLocation(), 10);
                turtle.getWorld().spawnParticle(Particle.FLAME, turtle.getLocation(), 10);
                audience.playSound(net.kyori.adventure.sound.Sound.sound(Key.key("entity.experience_orb.pickup"), net.kyori.adventure.sound.Sound.Source.PLAYER, 1.0f, 1.0f));
                turtle.remove();
            }
        }
    }
}
