package Shared.Api.Models.MembershipController;

import java.util.UUID;

public class UpdateMemberRoleInputModel {
    private UUID chatId;
    private UUID memberId;
    private String newRole;

    //<editor-fold desc="Getters and Setters">
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

    public String getNewRole() {
        return newRole;
    }

    public void setNewRole(String newRole) {
        this.newRole = newRole;
    }
    //</editor-fold>
}