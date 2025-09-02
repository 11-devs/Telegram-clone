package Shared.Api.Models.ChatController;

import java.util.UUID;

public class chatOutputModel {

    private UUID chatId;
    private String chatTitle;

    public chatOutputModel(){

    }
    public chatOutputModel(UUID chatId){
        this.chatId = chatId;
    }

    public UUID getChatId() {
        return chatId;
    }

    public void setChatId(UUID chatId) {
        this.chatId = chatId;
    }
}
