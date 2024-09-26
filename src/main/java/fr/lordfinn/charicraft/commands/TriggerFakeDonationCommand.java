package fr.lordfinn.charicraft.commands;

import fr.lordfinn.charicraft.Streamer;
import fr.lordfinn.charicraft.CharityDonationEvent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class TriggerFakeDonationCommand implements CommandExecutor {
    private final List<Streamer> streamers;
    private static final String PLAYER_ONLY_COMMAND_MESSAGE = "This command can only be run by a player.";
    private static final String INVALID_AMOUNT_MESSAGE = "Invalid amount. Please enter a valid number.";
    private static final String PROVIDE_AMOUNT_MESSAGE = "Please provide an amount.";
    private static final String DONATION_MESSAGE_SUFFIX = "Pour les nanimaux !";
    private double donationAmount = 0;

    public TriggerFakeDonationCommand(List<Streamer> streamers) {
        this.streamers = streamers;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(PLAYER_ONLY_COMMAND_MESSAGE);
            return false;
        }

        if (args.length > 0) {
            try {
                donationAmount = Double.parseDouble(args[0]);
                if (donationAmount < 0) {
                    donationAmount = 0;
                }
                sender.sendMessage("Donation amount set to: " + donationAmount);
                handleDonation(sender);
            } catch (NumberFormatException e) {
                sender.sendMessage(INVALID_AMOUNT_MESSAGE);
                return false;
            }
        } else {
            sender.sendMessage(PROVIDE_AMOUNT_MESSAGE);
            return false;
        }
        return true;
    }

    private void handleDonation(CommandSender sender) {
        String minecraftUsername = sender.getName();

        for (Streamer streamer : streamers) {
            if (streamer.getMinecraftUsername().equalsIgnoreCase(minecraftUsername)) {
                CharityDonationEvent.DonationMessage fakeDonation = new CharityDonationEvent.DonationMessage("John Doe", donationAmount, DONATION_MESSAGE_SUFFIX);
                streamer.handleDonation(fakeDonation);
                sender.sendMessage("Triggered fake donation for streamer: " + minecraftUsername);
                break;
            }
        }
    }
}