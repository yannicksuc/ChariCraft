package fr.lordfinn.charicraft;

import fr.lordfinn.charicraft.commands.FixDonationAmountCommand;

import java.util.List;

public class CharityDonationEvent {
    private String type; // "twitchcharitydonation"
    private List<DonationMessage> message; // List of donation messages
    private String forAccount; // "twitch_account"
    private String event_id; // Unique event ID

    public String getType() {
        return type;
    }

    public List<DonationMessage> getMessage() {
        return message;
    }

    CharityDonationEvent(int amount, String from, String message) {
        this.type = "";
        this.message = List.of(new DonationMessage(from, amount, message));
    }

    public static class DonationMessage {
        private String id;
        private String userId;
        private String from;
        private double amount;
        private String formattedAmount;
        private String currency;
        private String donationMessage;

        public DonationMessage(String from, double amount, String message) {
            this.from = from;
            this.amount = amount;
            this.donationMessage = message;
        }

        public String getFrom() {
            return from;
        }

        public double getAmount() {
            if (FixDonationAmountCommand.AMOUNT > 0)
                return FixDonationAmountCommand.AMOUNT;
            return amount;
        }

        public String getFormattedAmount() {
            return formattedAmount;
        }

        public String getCurrency() {
            return currency;
        }

        public String getDonationMessage() {
            return donationMessage;
        }
    }
}
