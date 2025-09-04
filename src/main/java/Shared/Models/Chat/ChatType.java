package Shared.Models.Chat;

public enum ChatType {
    PRIVATE ,
    GROUP ,
    CHANNEL ,
    SAVED_MESSAGES;

    public static ChatType fromString(String type) {
        try {
            return ChatType.valueOf(type.toUpperCase());
        } catch (Exception e) {
            return PRIVATE;
        }
    }
}
