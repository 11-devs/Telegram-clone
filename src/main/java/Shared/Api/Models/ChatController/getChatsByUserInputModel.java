package Shared.Api.Models.ChatController;

import java.util.UUID;

public class getChatsByUserInputModel {

    private UUID userId;

    public getChatsByUserInputModel(){

    }
    public getChatsByUserInputModel(UUID userId){
        this.userId = userId;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }
}
