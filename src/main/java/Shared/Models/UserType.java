package Shared.Models;

public enum UserType {
    USER,
    GROUP,
    SUPERGROUP,
    CHANNEL,
    ADMIN,
    SAVED_MESSAGES;

    public static UserType fromString(String type) {
        try {
            return UserType.valueOf(type.toUpperCase());
        } catch (Exception e) {
            return USER; // default
        }
    }

    @Override
    public String toString() {
        return name().toLowerCase();
    }
}
