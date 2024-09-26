package fr.lordfinn.charicraft.actions;

import fr.lordfinn.charicraft.CharityDonationEvent;
import fr.lordfinn.charicraft.Streamer;
import net.kyori.adventure.audience.Audience;

public interface IDonationAction {
    void execute(Streamer streamer, CharityDonationEvent.DonationMessage message, Audience audience);
}
