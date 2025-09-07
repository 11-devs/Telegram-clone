package Shared.Models;

import java.util.UUID;

public class ReplyInfo{
    public final UUID messageId;
    public final String senderName;
    public final String content;

    public ReplyInfo(UUID messageId, String senderName, String content) {
        this.messageId = messageId;
        this.senderName = senderName;
        this.content = content;
    }
}
