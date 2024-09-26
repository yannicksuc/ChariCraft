package fr.lordfinn.charicraft.actions;

import fr.lordfinn.charicraft.CharityDonationEvent;
import fr.lordfinn.charicraft.Streamer;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RandomEffectAction extends AbstractDonationAction {
    private static final int BADNESS_SCALE_FACTOR = 5;
    private static int globalEffectCounter = 0;
    private static final List<EffectWithTranslations> EFFECTS = new ArrayList<>();

    static {
        EFFECTS.add(new EffectWithTranslations(PotionEffectType.REGENERATION, List.of("Regeneration", "Régénération"), true));
        EFFECTS.add(new EffectWithTranslations(PotionEffectType.HEALTH_BOOST, List.of("Health Boost", "Bonus de vie"), false));
        EFFECTS.add(new EffectWithTranslations(PotionEffectType.INSTANT_HEALTH, List.of("Instant Health", "Soin instantané"), true));
        EFFECTS.add(new EffectWithTranslations(PotionEffectType.SATURATION, List.of("Saturation", "Saturation"), false));
        EFFECTS.add(new EffectWithTranslations(PotionEffectType.HASTE, List.of("Haste", "Célérité"), true));
        EFFECTS.add(new EffectWithTranslations(PotionEffectType.SPEED, List.of("Speed Boost", "Rapidité"), true));
        EFFECTS.add(new EffectWithTranslations(PotionEffectType.JUMP_BOOST, List.of("Jump Boost", "Sauts améliorés"), true));
        EFFECTS.add(new EffectWithTranslations(PotionEffectType.STRENGTH, List.of("Strength", "Force"), false));
        EFFECTS.add(new EffectWithTranslations(PotionEffectType.RESISTANCE, List.of("Resistance", "Résistance"), false));
        EFFECTS.add(new EffectWithTranslations(PotionEffectType.LUCK, List.of("Luck", "Chance"), false));
        EFFECTS.add(new EffectWithTranslations(PotionEffectType.NIGHT_VISION, List.of("Night Vision", "Nyctalopie"), true));
        EFFECTS.add(new EffectWithTranslations(PotionEffectType.GLOWING, List.of("Glowing", "Surbrillance"), false));
        EFFECTS.add(new EffectWithTranslations(PotionEffectType.ABSORPTION, List.of("Absorption", "Absorption"), true));
        EFFECTS.add(new EffectWithTranslations(PotionEffectType.HERO_OF_THE_VILLAGE, List.of("Hero of the Village", "Héros du village"), false));
        EFFECTS.add(new EffectWithTranslations(PotionEffectType.FIRE_RESISTANCE, List.of("Fire Resistance", "Résistance au feu"), true));
        EFFECTS.add(new EffectWithTranslations(PotionEffectType.WATER_BREATHING, List.of("Water Breathing", "Apnée"), true));
        EFFECTS.add(new EffectWithTranslations(PotionEffectType.INVISIBILITY, List.of("Invisibility", "Invisibilité"), false));
        EFFECTS.add(new EffectWithTranslations(PotionEffectType.CONDUIT_POWER, List.of("Conduit Power", "Force de conduit"), false));
        EFFECTS.add(new EffectWithTranslations(PotionEffectType.DOLPHINS_GRACE, List.of("Dolphin's Grace", "Grâce du dauphin"), false));
        EFFECTS.add(new EffectWithTranslations(PotionEffectType.LEVITATION, List.of("Levitation", "Lévitation"), true));
        EFFECTS.add(new EffectWithTranslations(PotionEffectType.SLOW_FALLING, List.of("Slow Falling", "Chute lente"), true));
        EFFECTS.add(new EffectWithTranslations(PotionEffectType.UNLUCK, List.of("Bad Luck", "Malchance"), false));
        EFFECTS.add(new EffectWithTranslations(PotionEffectType.BLINDNESS, List.of("Blindness", "Cécité"), true));
        EFFECTS.add(new EffectWithTranslations(PotionEffectType.BAD_OMEN, List.of("Bad Omen", "Mauvais présage"), false));
        EFFECTS.add(new EffectWithTranslations(PotionEffectType.SLOWNESS, List.of("Slowness", "Lenteur"), true));
        EFFECTS.add(new EffectWithTranslations(PotionEffectType.WEAKNESS, List.of("Weakness", "Faiblesse"), true));
        EFFECTS.add(new EffectWithTranslations(PotionEffectType.DARKNESS, List.of("Darkness", "Obscurité"), true));
        EFFECTS.add(new EffectWithTranslations(PotionEffectType.NAUSEA, List.of("Nausea", "Nausée"), true));
        EFFECTS.add(new EffectWithTranslations(PotionEffectType.HUNGER, List.of("Hunger", "Faim"), true));
        EFFECTS.add(new EffectWithTranslations(PotionEffectType.POISON, List.of("Poison", "Poison"), true));
        EFFECTS.add(new EffectWithTranslations(PotionEffectType.INSTANT_DAMAGE, List.of("Instant Damage", "Dégâts instantanés"), false));
        EFFECTS.add(new EffectWithTranslations(PotionEffectType.MINING_FATIGUE, List.of("Mining Fatigue", "Fatigue"), true));
        EFFECTS.add(new EffectWithTranslations(PotionEffectType.WITHER, List.of("Wither", "Wither"), true));
    }

    public RandomEffectAction() {
        super(Duration.ofSeconds(globalEffectCounter), "Effet Aléatoire", "Applique un effet aléatoire à tous les joueurs", BossBar.Color.PURPLE);
    }

    @Override
    protected void onEnd(Streamer streamer, CharityDonationEvent.DonationMessage message, Audience audience) {
        super.onEnd(streamer, message, audience);
    }

    @Override
    protected void onStart(Streamer streamer, CharityDonationEvent.DonationMessage message, Audience audience) {
        super.onStart(streamer, message, audience);

        PotionEffectType potionEffectType = fetchTranslatedEffectName(message.getFrom());
        if (potionEffectType == null) {
            potionEffectType = selectRandomEffectType();
        }
        applyEffectToAllPlayers(potionEffectType, audience, globalEffectCounter / 5);

        globalEffectCounter++;
    }

    private PotionEffectType fetchTranslatedEffectName(String effectName) {
        for (EffectWithTranslations effectTranslation : EFFECTS) {
            if (effectTranslation.isTranslationPresent(effectName)) {
                return effectTranslation.effect();
            }
        }
        return null;
    }

    private PotionEffectType selectRandomEffectType() {
        Random random = new Random();
        int bound = EFFECTS.size();
        int scaledBound = bound + globalEffectCounter / BADNESS_SCALE_FACTOR;
        int index = random.nextInt(scaledBound);
        int step = 8;
        int stepLimiter = 0;

        while (index >= (bound - stepLimiter) && stepLimiter < bound) {
            scaledBound = (scaledBound + (bound - stepLimiter)) / 2;
            index = random.nextInt(scaledBound);
            if (!(index >= (bound - stepLimiter)))
                stepLimiter += step;
        }

        int finalIndex = Math.min(index + stepLimiter, bound - 1);
        return EFFECTS.get(finalIndex).effect();
    }

    private void applyPotionEffect(Player player, PotionEffect potionEffect) {
        player.addPotionEffect(potionEffect);
    }

    private void applyEffectToAllPlayers(PotionEffectType effectType, Audience audience, int effectLevel) {
        PotionEffect potionEffect = new PotionEffect(effectType, (int) duration.toMillis(), effectLevel);
        String effectNameInFrench = getEffectNameInFrench(effectType);
        Component title = Component.text(effectNameInFrench).color(NamedTextColor.DARK_RED);
        Component subtitle = Component.text("Niveau " + effectLevel).color(NamedTextColor.RED);

        Title titleComponent = Title.title(title, subtitle, Title.Times.times(Duration.ofMillis(500), Duration.ofSeconds(3), Duration.ofMillis(1000)));
        audience.forEachAudience(member -> {
            if (member instanceof Player player) {
                player.showTitle(titleComponent);
                applyPotionEffect(player, potionEffect);
            }
        });
    }

    private String getEffectNameInFrench(PotionEffectType effectType) {
        for (EffectWithTranslations effectTranslation : EFFECTS) {
            if (effectTranslation.effect().equals(effectType)) {
                // Assuming that the second element in translations is the French name
                return effectTranslation.translations().get(1);
            }
        }
        return "Effet Inconnu"; // Default if translation is not found
    }

    private record EffectWithTranslations(PotionEffectType effect, List<String> translations, boolean isRelevant) {

        public boolean isTranslationPresent(String translation) {
            return translations.stream().anyMatch(translation::equalsIgnoreCase);
        }
    }
}