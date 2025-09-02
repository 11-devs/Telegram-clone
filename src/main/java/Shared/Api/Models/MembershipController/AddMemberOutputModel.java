package Shared.Api.Models.MembershipController;

import Shared.Models.Membership.Membership;

public class AddMemberOutputModel {
    private Membership membership;

    public Membership getMembership() {
        return membership;
    }

    public void setMembership(Membership membership) {
        this.membership = membership;
    }
}