package fr.lordfinn.charicraft.actions;

import fr.lordfinn.charicraft.ChariCraft;
import fr.lordfinn.charicraft.CharityDonationEvent;
import fr.lordfinn.charicraft.Streamer;
import fr.lordfinn.charicraft.utils.Countdown;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Camel;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.plugin.PluginManager;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class FriendsLikeCamelsAction extends AbstractDonationAction implements Listener {
    private static final double CAMEL_SPEED = 0.7;
    private static final double CAMEL_JUMP_STRENGTH = 3.0;
    private static final double PLAYER_RANGE = 50;
    private static final long TASK_PERIOD = 200L;
    private static final int EFFECT_DURATION_TICKS = 200; // 10 secondes

    private BukkitTask effectTask;
    private Camel camel;
    private Player firstPlayer;
    private Player secondPlayer;

    public FriendsLikeCamelsAction() {
        super(Duration.ofMinutes(10), "Copains comme chameaux", "Pas sûr de l'animal, mais dans le doute restez sur lui", BossBar.Color.YELLOW);

        // Register this class as an event listener
        PluginManager pluginManager = ChariCraft.getInstance().getServer().getPluginManager();
        pluginManager.registerEvents(this, ChariCraft.getInstance());
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
        Countdown.start(firstPlayer, 10, 0, NamedTextColor.RED, "The floor is lava");
        Countdown.start(secondPlayer, 10, 0, NamedTextColor.RED, "The floor is lava");
        teleportPlayersToLocation(loc);
        summonInvulnerableCamel(world, loc);
        setPlayerAttributes((int) PLAYER_RANGE);

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
        camel.setTamed(true);
        camel.getInventory().setSaddle(new ItemStack(Material.SADDLE));
        Objects.requireNonNull(camel.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED)).setBaseValue(CAMEL_SPEED);
        Objects.requireNonNull(camel.getAttribute(Attribute.GENERIC_JUMP_STRENGTH)).setBaseValue(CAMEL_JUMP_STRENGTH);
    }

    private void setPlayerAttributes(int range) {
        Objects.requireNonNull(firstPlayer.getAttribute(Attribute.PLAYER_ENTITY_INTERACTION_RANGE)).setBaseValue(range);
        Objects.requireNonNull(secondPlayer.getAttribute(Attribute.PLAYER_ENTITY_INTERACTION_RANGE)).setBaseValue(range);
        Objects.requireNonNull(firstPlayer.getAttribute(Attribute.PLAYER_BLOCK_INTERACTION_RANGE)).setBaseValue(range);
        Objects.requireNonNull(secondPlayer.getAttribute(Attribute.PLAYER_BLOCK_INTERACTION_RANGE)).setBaseValue(range);
    }

    private void startEffectTask() {
        effectTask = new BukkitRunnable() {
            @Override
            public void run() {
                handlePlayerEffects(firstPlayer);
                handlePlayerEffects(secondPlayer);
            }
        }.runTaskTimer(ChariCraft.getInstance(), TASK_PERIOD, 20L);
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
        player.addPotionEffect(new PotionEffect(PotionEffectType.INSTANT_DAMAGE, 1, 0));
    }

    private void removeNegativeEffects(Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, EFFECT_DURATION_TICKS, 100));
        player.removePotionEffect(PotionEffectType.WITHER);
        player.removePotionEffect(PotionEffectType.POISON);
        player.removePotionEffect(PotionEffectType.NAUSEA);
        player.removePotionEffect(PotionEffectType.INSTANT_DAMAGE);
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player respawnedPlayer = event.getPlayer();
        if (respawnedPlayer.equals(firstPlayer) || respawnedPlayer.equals(secondPlayer)) {
            ChariCraft.getInstance().getServer().getScheduler().runTaskLater(ChariCraft.getInstance(), () -> {
                Location respawnLocation = respawnedPlayer.getLocation();
                camel.teleport(respawnLocation);
                if (respawnedPlayer.equals(firstPlayer)) {
                    secondPlayer.teleport(respawnLocation);
                    camel.addPassenger(secondPlayer);
                } else {
                    firstPlayer.teleport(respawnLocation);
                    camel.addPassenger(firstPlayer);
                }
                camel.addPassenger(respawnedPlayer);
            }, 1L);
        }
    }

    @Override
    public void onEnd(Streamer streamer, CharityDonationEvent.DonationMessage message, Audience audience) {
        if (effectTask != null)
            effectTask.cancel();
        setPlayerAttributes(3);
        ChariCraft.getInstance().getLogger().info("Copain comme chameau est terminé. Les ccons peuvent descendre du chameau.");
    }
}