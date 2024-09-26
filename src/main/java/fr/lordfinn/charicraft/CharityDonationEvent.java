import java.util.List;

public class CharityDonationEvent {
    private String eventType; // "event"
    private EventData data; // The second object in the array

    public String getEventType() {
        return eventType;
    }

    public EventData getData() {
        return data;
    }

    public static class EventData {
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

        public static class DonationMessage {
            private String id;
            private String userId;
            private String from;
            private double amount;
            private String formattedAmount;
            private String currency;

            public String getFrom() {
                return from;
            }

            public double getAmount() {
                return amount;
            }

            public String getFormattedAmount() {
                return formattedAmount;
            }

            public String getCurrency() {
                return currency;
            }
        }
    }
}
