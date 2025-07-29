package Shared.Database.Repository;

import Shared.Database.DAO.GenericDAO;
import Shared.Models.Media.Media;
import Shared.Models.Membership.Membership;

import java.util.UUID;


public class MembershipRepository extends Repository<Membership, UUID> {
    public MembershipRepository(GenericDAO<Membership> primaryDAO, GenericDAO<Membership> cacheDAO) {
        super(Membership.class, primaryDAO, cacheDAO);
    }
}