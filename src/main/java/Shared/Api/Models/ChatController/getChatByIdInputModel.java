package Shared.Api.Models.ChatController;

import java.util.UUID;

public class getChatByIdInputModel {

    private UUID chatId;

    public getChatByIdInputModel(){

    }
    public getChatByIdInputModel(UUID chatId){
        this.chatId = chatId;
    }

    public UUID getChatId() {
        return chatId;
    }

    public void setChatId(UUID chatId) {
        this.chatId = chatId;
    }
}
