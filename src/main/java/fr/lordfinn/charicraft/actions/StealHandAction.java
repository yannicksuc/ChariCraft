package fr.lordfinn.charicraft.actions;

import fr.lordfinn.charicraft.CharityDonationEvent;
import fr.lordfinn.charicraft.Streamer;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Allay;
import org.bukkit.entity.Bat;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.*;

public class StealHand extends AbstractDonationAction {
    private static final double BAT_FLYING_SPEED = 4.0;
    private static final double ALLAY_SCALE = 6.0;
    private static final String ALLAY_CUSTOM_NAME = "Cheh!";
    private static final int LEVITATION_DURATION = 500;
    private static final int LEVITATION_AMPLIFIER = 10;

    private Queue<Bat> bats = new LinkedList<>();
    private Queue<Allay> allays = new LinkedList<>();

    public StealHand() {
        super(Duration.ofMinutes(2), "Allay, à plus !", "T'as compris la blague ? Ca n'a pas l'air d'allay...", BossBar.Color.BLUE);
    }

    @Override
    public void onStart(Streamer streamer, CharityDonationEvent.DonationMessage message, Audience audience) {
        notifyAudience(audience);
        playSoundForAudience(audience);
        audience.forEachAudience(player -> summonAllayWithBatSafely((Player) player));
    }

    private void notifyAudience(Audience audience) {
        audience.sendMessage(Component.text(this.description));
    }

    private void playSoundForAudience(Audience audience) {
        audience.playSound(Sound.sound(Key.key("entity.evoker.prepare_wololo"), Sound.Source.PLAYER, 1.0f, 1.0f));
    }

    private void summonAllayWithBatSafely(Player player) {
        try {
            summonAllayWithBat(player);
        } catch (ClassCastException ignored) {
        }
    }

    @Override
    public void onEnd(Streamer streamer, CharityDonationEvent.DonationMessage message, Audience audience) {
        while (entitiesAreAvailable()) {
            Allay allay = allays.poll();
            Bat bat = bats.poll();
            if (checkEntitiesValidity(allay, bat)) {
                handleEntityEnd(allay, bat);
            }
        }
    }

    private boolean entitiesAreAvailable() {
        return !allays.isEmpty() && !bats.isEmpty();
    }

    private void handleEntityEnd(Allay allay, Bat bat) {
        Location allayLocation = allay.getLocation();
        createFireworkEffect(allayLocation, allay.getWorld());
        dropHeldItem(allay);
        removeEntities(allay, bat);
    }

    private boolean checkEntitiesValidity(Allay allay, Bat bat) {
        return allay != null && bat != null && allay.isValid() && bat.isValid();
    }

    private void createFireworkEffect(Location location, @NotNull World world) {
        FireworkEffect effect = FireworkEffect.builder()
                .withColor(Color.BLUE, Color.AQUA, Color.AQUA, Color.WHITE)
                .with(FireworkEffect.Type.BALL_LARGE)
                .withFlicker()
                .build();
        Firework firework = world.spawn(location, Firework.class);
        FireworkMeta meta = firework.getFireworkMeta();
        meta.addEffect(effect);
        firework.setFireworkMeta(meta);
        firework.detonate();
    }

    private void dropHeldItem(Allay allay) {
        ItemStack heldItem = allay.getEquipment().getItemInMainHand();
        if (heldItem != null && heldItem.getAmount() > 0) {
            allay.getWorld().dropItemNaturally(allay.getLocation(), heldItem);
        }
    }

    private void removeEntities(Allay allay, Bat bat) {
        allay.remove();
        bat.remove();
    }

    private void summonAllayWithBat(Player player) {
        Location spawnLocation = getSpawnLocation(player);
        Bat bat = summonInvisibleBat(player, spawnLocation);
        Allay allay = summonAllay(player, spawnLocation);
        handleHeldItem(player, allay);
        bat.addPassenger(allay);
    }

    private void handleHeldItem(Player player, Allay allay) {
        ItemStack heldItem = getHeldItemOrRandom(player);
        if (isValidItem(heldItem)) {
            giveItemToAllay(allay, heldItem, player);
        }
    }

    private Location getSpawnLocation(Player player) {
        return player.getLocation().add(0, 1, 0);
    }

    private Bat summonInvisibleBat(Player player, Location spawnLocation) {
        Bat bat = player.getWorld().spawn(spawnLocation, Bat.class);
        bat.setInvisible(true);
        Objects.requireNonNull(bat.getAttribute(Attribute.GENERIC_FLYING_SPEED)).setBaseValue(BAT_FLYING_SPEED);
        bat.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, LEVITATION_DURATION, LEVITATION_AMPLIFIER));
        bat.setVelocity(new Vector(0, 3, 0));
        bats.add(bat);
        return bat;
    }

    private Allay summonAllay(Player player, Location spawnLocation) {
        Allay allay = player.getWorld().spawn(spawnLocation, Allay.class);
        Objects.requireNonNull(allay.getAttribute(Attribute.GENERIC_SCALE)).setBaseValue(ALLAY_SCALE);
        allay.customName(Component.text(ALLAY_CUSTOM_NAME));
        allay.setCustomNameVisible(true);
        allays.add(allay);
        return allay;
    }

    private ItemStack getHeldItemOrRandom(Player player) {
        ItemStack heldItem = player.getInventory().getItemInMainHand();
        if (heldItem.getAmount() == 0) {
            heldItem = getRandomHeldItem(player);
        }
        return heldItem;
    }

    private ItemStack getRandomHeldItem(Player player) {
        List<ItemStack> nonEmptyItems = Arrays.stream(player.getInventory().getContents())
                .filter(item -> item != null && item.getAmount() > 0)
                .toList();
        if (!nonEmptyItems.isEmpty()) {
            return nonEmptyItems.get((int) (Math.random() * nonEmptyItems.size()));
        }
        return null;
    }

    private boolean isValidItem(ItemStack item) {
        return item != null && item.getAmount() > 0;
    }

    private void giveItemToAllay(Allay allay, ItemStack heldItem, Player player) {
        allay.setCanPickupItems(true);
        allay.startDancing();
        allay.getEquipment().setItemInMainHand(heldItem);
        player.getInventory().remove(heldItem);
    }
}