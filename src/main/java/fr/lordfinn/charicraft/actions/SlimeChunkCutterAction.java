package fr.lordfinn.charicraft.actions;

import fr.lordfinn.charicraft.ChariCraft;
import fr.lordfinn.charicraft.CharityDonationEvent;
import fr.lordfinn.charicraft.Streamer;
import fr.lordfinn.charicraft.utils.Countdown;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.time.Duration;
import java.util.LinkedList;
import java.util.Queue;
import java.util.UUID;

public class SlimeChunkCutterAction extends AbstractDonationAction {

    private final Queue<UUID> turtleQueue = new LinkedList<>();

    public SlimeChunkCutterAction() {
        super(Duration.ofMillis(5000), "Slime Chunk Cutter", "C'est carré'", BossBar.Color.GREEN);
    }

    @Override
    public void onStart(Streamer streamer, CharityDonationEvent.DonationMessage message, Audience audience) {
        super.onStart(streamer, message, audience);
        audience.forEachAudience(player -> {
            if (player instanceof Player) {
                Location center = ((Player) player).getLocation();
                int x1 = center.getBlockX() - 25;
                int x2 = center.getBlockX() + 24;
                int z1 = center.getBlockZ() - 25;
                int z2 = center.getBlockZ() + 24;
                int yPlayer = center.getBlockY();

                // Cut the top part
                this.cutSection(center.getWorld(), x1, x2, yPlayer + 1, 320, z1, z2);

                // Replace non-air walls with slime blocks
                this.replaceWalls(center.getWorld(), x1 - 1, x2 + 1, yPlayer + 1, 320, z1 - 1, z2 + 1);

                // Summon slime
                Location slimeLocation = center.clone().add(0, 30, 0);
                Slime slime = (Slime) center.getWorld().spawnEntity(slimeLocation, EntityType.SLIME);
                slime.setAI(false);
                slime.setSize(70);
                slime.setRotation(0, 0);
                slime.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 20 * 20, 80)); // Resistance for 20 seconds
                slime.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 20 * 20, 1)); // Regeneration for 20 seconds
                slime.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 20 * 3, 1)); // Slow Falling for 3 seconds

                Countdown.start((Player) player, 3, 0, NamedTextColor.DARK_GREEN, "↑↑↑");

                // Summon an Armor Stand after 3 seconds
                Bukkit.getScheduler().runTaskLater(ChariCraft.getInstance(), () -> {
                    Location armorStandLocation = slime.getLocation();
                    ArmorStand armorStand = (ArmorStand) center.getWorld().spawnEntity(armorStandLocation, EntityType.ARMOR_STAND);
                    armorStand.setInvulnerable(true);
                    armorStand.addPassenger(slime);
                    slime.setAI(false);
                    this.cutSection(center.getWorld(), x1, x2, -64, yPlayer, z1, z2);
                    this.replaceWalls(center.getWorld(), x1 - 1, x2 + 1, -64, yPlayer, z1 - 1, z2 + 1);

                    Bukkit.getScheduler().runTaskLater(ChariCraft.getInstance(), () -> {
                        armorStand.remove();
                        slime.setAI(true);
                        slime.setHealth(70);
                        slime.setPersistent(true);
                    }, 200L); // 200 ticks = 10 seconds
                }, 60L); // 60 ticks = 3 seconds
            }
        });
    }

    private void cutSection(World world, int x1, int x2, int y1, int y2, int z1, int z2) {
        for (int x = x1; x <= x2; x++) {
            for (int y = y1; y <= y2; y++) {
                for (int z = z1; z <= z2; z++) {
                    Block block = world.getBlockAt(x, y, z);
                    if (block.getType() != Material.BEDROCK) {
                        block.setType(Material.AIR);
                    }
                }
            }
        }
    }

    private void replaceWalls(World world, int x1, int x2, int y1, int y2, int z1, int z2) {
        for (int x = x1; x <= x2; x++) {
            for (int y = y1; y <= y2; y++) {
                Block block1 = world.getBlockAt(x, y, z1);
                if (block1.getType() != Material.AIR) {
                    block1.setType(Material.SLIME_BLOCK);
                }
                Block block2 = world.getBlockAt(x, y, z2);
                if (block2.getType() != Material.AIR) {
                    block2.setType(Material.SLIME_BLOCK);
                }
            }
        }
        for (int z = z1; z <= z2; z++) {
            for (int y = y1; y <= y2; y++) {
                Block block1 = world.getBlockAt(x1, y, z);
                if (block1.getType() != Material.AIR) {
                    block1.setType(Material.SLIME_BLOCK);
                }
                Block block2 = world.getBlockAt(x2, y, z);
                if (block2.getType() != Material.AIR) {
                    block2.setType(Material.SLIME_BLOCK);
                }
            }
        }
    }

    @Override
    public void onEnd(Streamer streamer, CharityDonationEvent.DonationMessage message, Audience audience) {
    }
}