package fr.lordfinn.charicraft.actions;

import fr.lordfinn.charicraft.ChariCraft;
import fr.lordfinn.charicraft.CharityDonationEvent;
import fr.lordfinn.charicraft.Streamer;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.key.Key;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.util.Vector;

import java.time.Duration;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Queue;
import java.util.UUID;

public class MineTurtleAction extends AbstractDonationAction {

    private final Queue<UUID> turtleQueue = new LinkedList<>();

    public MineTurtleAction() {
        super(Duration.ofMillis(3500), "Mine Turtle", "Hello !", BossBar.Color.GREEN);
    }

    @Override
    public void onStart(Streamer streamer, CharityDonationEvent.DonationMessage message, Audience audience) {
        audience.forEachAudience(player -> {
            Player bukkitPlayer = (Player) player;
            if (bukkitPlayer != null) {
                spawnAndPrepareEntities(bukkitPlayer);
            }
        });
        playStartSounds(audience);
    }

    private void spawnAndPrepareEntities(Player bukkitPlayer) {
        Turtle turtle = bukkitPlayer.getWorld().spawn(bukkitPlayer.getLocation(), Turtle.class);
        Creeper creeper = (Creeper) bukkitPlayer.getWorld().spawnEntity(bukkitPlayer.getLocation(), EntityType.CREEPER);
        prepareEntities(turtle, creeper);
        turtleQueue.add(turtle.getUniqueId());
    }

    private void prepareEntities(Turtle turtle, Creeper creeper) {
        turtle.addPassenger(creeper);
        turtle.setInvulnerable(true);
        Objects.requireNonNull(turtle.getAttribute(Attribute.GENERIC_SCALE)).setBaseValue(0.5);
        Objects.requireNonNull(turtle.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED)).setBaseValue(0);
        creeper.setAI(false);
        creeper.setPowered(true);
        Objects.requireNonNull(creeper.getAttribute(Attribute.GENERIC_SCALE)).setBaseValue(0.3);
    }

    private void playStartSounds(Audience audience) {
        audience.playSound(net.kyori.adventure.sound.Sound.sound(Key.key("entity.creeper.primed"), net.kyori.adventure.sound.Sound.Source.PLAYER, 1.0f, 1.0f));
        audience.playSound(net.kyori.adventure.sound.Sound.sound(Key.key("actions.mineturtle"), net.kyori.adventure.sound.Sound.Source.PLAYER, 1.0f, 1.0f));
    }

    @Override
    public void onEnd(Streamer streamer, CharityDonationEvent.DonationMessage message, Audience audience) {
        while (!turtleQueue.isEmpty()) {
            triggerTurtle(turtleQueue.poll());
        }
    }

    private void triggerTurtle(UUID turtleUuid) {
        Turtle turtle = (Turtle) Bukkit.getEntity(turtleUuid);
        if (turtle != null) {
            if (!turtle.getPassengers().isEmpty()) {
                turtle.getPassengers().forEach(Entity::remove);
            }
            createExplosion(turtle);
        }
    }

    private void createExplosion(Turtle turtle) {
        World world = turtle.getWorld();
        Location location = turtle.getLocation();
        double explosionRadius = 10.0;
        dealDamageAndPushPlayers(world, location, explosionRadius);
        breakBlocksInSphere(world, location, explosionRadius);
        spawnExplosionParticles(world, location);
        Objects.requireNonNull(turtle.getAttribute(Attribute.GENERIC_SCALE)).setBaseValue(1);
        Bukkit.getScheduler().runTaskTimer(ChariCraft.getInstance(), new HappyParticleEffectRunnable(world, turtle), 30, 20);
    }

    private void spawnExplosionParticles(World world, Location location) {
        world.spawnParticle(Particle.EXPLOSION, location, 10);
        world.spawnParticle(Particle.FLAME, location, 10);
    }

    private static class HappyParticleEffectRunnable implements Runnable {
        private final World world;
        private final Turtle turtle;
        private int ticks = 0;

        public HappyParticleEffectRunnable(World world, Turtle turtle) {
            this.world = world;
            this.turtle = turtle;
        }

        @Override
        public void run() {
            if (ticks >= 120) { // 4 seconds * 20 ticks per second
                this.cancel();
                return;
            }
            world.spawnParticle(Particle.HEART, turtle.getLocation().clone().add(0, 1, 0), 10, 0.5, 0.5, 0.5, 0);
            world.spawnParticle(Particle.WAX_OFF, turtle.getLocation().clone().add(0, 1, 0), 10, 0.5, 0.5, 0.5, 0);
            ticks += 20;
        }

        private void cancel() {
            Bukkit.getScheduler().cancelTask(this.hashCode());
        }
    }

    private void dealDamageAndPushPlayers(World world, Location location, double explosionRadius) {
        world.getNearbyEntities(location, explosionRadius, explosionRadius, explosionRadius).forEach(entity -> {
            if (entity instanceof Player player) {
                double distance = player.getLocation().distance(location);
                double damage = distance == 0 ? 40 : 20 * (1 - distance / explosionRadius);
                player.damage(damage);
                Vector direction = player.getLocation().toVector().subtract(location.toVector()).normalize();
                direction.setY(1);
                double factor = 10 * (1 - distance / explosionRadius);
                player.setVelocity(player.getVelocity().add(direction.multiply(factor)));
            }
        });
    }

    private void breakBlocksInSphere(World world, Location location, double explosionRadius) {
        for (int x = -10; x <= 10; x++) {
            for (int y = -10; y <= 10; y++) {
                for (int z = -10; z <= 10; z++) {
                    Location blockLocation = location.clone().add(x, y, z);
                    double distance = blockLocation.distance(location);
                    if (distance <= explosionRadius) {
                        if (distance > explosionRadius * 0.8 && Math.random() > (1 - (distance / explosionRadius))) {
                            continue;
                        }
                        Block block = world.getBlockAt(blockLocation);
                        if (!block.isEmpty()) {
                            block.breakNaturally();
                        }
                    }
                }
            }
        }
    }
}