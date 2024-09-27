package fr.lordfinn.charicraft.actions;

import fr.lordfinn.charicraft.ChariCraft;
import fr.lordfinn.charicraft.CharityDonationEvent;
import fr.lordfinn.charicraft.Streamer;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.bossbar.BossBar;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Camel;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class FriendsLikeCamelsAction extends AbstractDonationAction {
    private static final double CAMEL_SPEED = 0.7;
    private static final double CAMEL_JUMP_STRENGTH = 3.0;
    private static final double PLAYER_RANGE = 10.0;
    private static final long TASK_PERIOD = 200L;
    private static final int EFFECT_DURATION_TICKS = 200; // 10 secondes

    private @NotNull BukkitTask effectTask;
    private Camel camel;
    private Player firstPlayer;
    private Player secondPlayer;

    public FriendsLikeCamelsAction() {
        super(Duration.ofMinutes(10), "Copains comme chameaux", "Pas sûr de l'animal, mais dans le doute restez sur lui", BossBar.Color.YELLOW);
    }

    @Override
    public void onStart(Streamer streamer, CharityDonationEvent.DonationMessage message, Audience audience) {
        ChariCraft.getInstance().getLogger().info("NAME " + streamer.getMinecraftUsername());

        World world = streamer.getBukkitPlayer().getWorld();
        Location loc = streamer.getBukkitPlayer().getLocation();
        List<Player> players = world.getPlayers();
        if (players.size() < 2) {
            ChariCraft.getInstance().getLogger().severe("Not enough players on the server.");
            return;
        }

        selectTwoRandomPlayers(players);
        teleportPlayersToLocation(loc);
        summonInvulnerableCamel(world, loc);
        setPlayerAttributes();

        startEffectTask();
    }

    private void selectTwoRandomPlayers(List<Player> players) {
        Random random = new Random();
        firstPlayer = players.get(random.nextInt(players.size()));
        do {
            secondPlayer = players.get(random.nextInt(players.size()));
        } while (firstPlayer.equals(secondPlayer));
    }

    private void teleportPlayersToLocation(Location loc) {
        firstPlayer.teleport(loc);
        secondPlayer.teleport(loc);
    }

    private void summonInvulnerableCamel(World world, Location loc) {
        camel = world.spawn(loc, Camel.class);
        camel.setInvulnerable(true);
        Objects.requireNonNull(camel.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED)).setBaseValue(CAMEL_SPEED);
        Objects.requireNonNull(camel.getAttribute(Attribute.GENERIC_JUMP_STRENGTH)).setBaseValue(CAMEL_JUMP_STRENGTH);
    }

    private void setPlayerAttributes() {
        Objects.requireNonNull(firstPlayer.getAttribute(Attribute.PLAYER_ENTITY_INTERACTION_RANGE)).setBaseValue(PLAYER_RANGE);
        Objects.requireNonNull(secondPlayer.getAttribute(Attribute.PLAYER_ENTITY_INTERACTION_RANGE)).setBaseValue(PLAYER_RANGE);
        Objects.requireNonNull(firstPlayer.getAttribute(Attribute.PLAYER_BLOCK_INTERACTION_RANGE)).setBaseValue(PLAYER_RANGE);
        Objects.requireNonNull(secondPlayer.getAttribute(Attribute.PLAYER_BLOCK_INTERACTION_RANGE)).setBaseValue(PLAYER_RANGE);
    }

    private void startEffectTask() {
        effectTask = new BukkitRunnable() {
            @Override
            public void run() {
                handlePlayerEffects(firstPlayer);
                handlePlayerEffects(secondPlayer);
            }
        }.runTaskTimer(ChariCraft.getInstance(), 0L, TASK_PERIOD);
    }

    private void handlePlayerEffects(Player player) {
        if (!player.isInsideVehicle() || player.getVehicle() != camel) {
            applyNegativeEffects(player);
        } else {
            removeNegativeEffects(player);
        }
    }

    private void applyNegativeEffects(Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, EFFECT_DURATION_TICKS, 2));
        player.addPotionEffect(new PotionEffect(PotionEffectType.POISON, EFFECT_DURATION_TICKS, 1));
        player.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, EFFECT_DURATION_TICKS, 10));
        player.addPotionEffect(new PotionEffect(PotionEffectType.INSTANT_DAMAGE, 1, 0)); // Dégât instantané
    }

    private void removeNegativeEffects(Player player) {
        player.removePotionEffect(PotionEffectType.WITHER);
        player.removePotionEffect(PotionEffectType.POISON);
        player.removePotionEffect(PotionEffectType.NAUSEA);
        player.removePotionEffect(PotionEffectType.INSTANT_DAMAGE);
    }

    public void onEnd(Streamer streamer, CharityDonationEvent.DonationMessage message, Audience audience) {
        effectTask.cancel();
        ChariCraft.getInstance().getLogger().info("Copain comme chameau est terminé. Les ccons peuvent descendre du chameau.");
    }

    private void dropItems(Player player) {
        // Implementer la logique pour faire tomber les objets
    }
}