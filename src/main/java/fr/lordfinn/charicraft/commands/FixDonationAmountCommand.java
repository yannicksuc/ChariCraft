package fr.lordfinn.charicraft.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class FixDonationAmountCommand implements CommandExecutor {
    public static double AMOUNT = 0;

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (args.length > 0) {
            try {
                AMOUNT = Double.parseDouble(args[0]);
                if (AMOUNT < 0) {
                    AMOUNT = 0;
                }
                sender.sendMessage("Donation amount set to: " + AMOUNT);
            } catch (NumberFormatException e) {
                sender.sendMessage("Invalid amount. Please enter a valid number.");
                return false;
            }
        } else {
            sender.sendMessage("Please provide an amount.");
            return false;
        }
        return true;
    }
}
