package Shared.Api.Models.MembershipController;

import java.util.List;

public class GetChatMembersOutputModel {
    private List<MemberInfo> members;

    public GetChatMembersOutputModel() {}

    public GetChatMembersOutputModel(List<MemberInfo> members) {
        this.members = members;
    }

    public List<MemberInfo> getMembers() {
        return members;
    }

    public void setMembers(List<MemberInfo> members) {
        this.members = members;
    }
}