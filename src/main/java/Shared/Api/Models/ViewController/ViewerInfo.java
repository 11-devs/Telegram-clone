package Shared.Api.Models.ViewController;

import java.util.UUID;

public class ViewerInfo {
    private UUID userId;
    private String firstName;
    private String lastName;

    public ViewerInfo(UUID userId, String firstName, String lastName) {
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }
}