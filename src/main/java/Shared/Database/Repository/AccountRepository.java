package Shared.Database.Repository;

import Shared.Database.DAO.GenericDAO;
import Shared.Models.Account.Account;

import java.util.UUID;

public class AccountRepository extends Repository<Account, UUID> {
    public AccountRepository(GenericDAO<Account> primaryDAO, GenericDAO<Account> cacheDAO) {
        super(Account.class, primaryDAO, cacheDAO);
    }
}