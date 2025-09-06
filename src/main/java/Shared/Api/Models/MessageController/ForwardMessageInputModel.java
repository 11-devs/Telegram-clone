package Shared.Api.Models.MessageController;

import java.util.List;
import java.util.UUID;

public class ForwardMessageInputModel {
    private UUID messageToForwardId;
    private List<UUID> targetChatIds;

    public UUID getMessageToForwardId() {
        return messageToForwardId;
    }

    public void setMessageToForwardId(UUID messageToForwardId) {
        this.messageToForwardId = messageToForwardId;
    }

    public List<UUID> getTargetChatIds() {
        return targetChatIds;
    }

    public void setTargetChatIds(List<UUID> targetChatIds) {
        this.targetChatIds = targetChatIds;
    }
}