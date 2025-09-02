package Shared.Api.Models.MembershipController;

import java.util.UUID;

public class KickMemberInputModel {
    private UUID chatId;
    private UUID memberId;
    private UUID kickerId;

    public UUID getChatId() {
        return chatId;
    }

    public void setChatId(UUID chatId) {
        this.chatId = chatId;
    }

    public UUID getMemberId() {
        return memberId;
    }

    public void setMemberId(UUID memberId) {
        this.memberId = memberId;
    }

    public UUID getKickerId() {
        return kickerId;
    }

    public void setKickerId(UUID kickerId) {
        this.kickerId = kickerId;
    }
}