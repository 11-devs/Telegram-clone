package Shared.Database.Example;

import Shared.Database.Database;
import Shared.Database.SQLiteDatabase;
import Shared.Utils.Console;
import Shared.Database.DAO.GenericDAO;
import Shared.Models.Account.*;

import java.util.List;

public class Example {

    public static void main(String[] args) {
        Console.clear();
        Database primaryDatabase = new Database();
        SQLiteDatabase localDatabase = new SQLiteDatabase();


        // Server Usage:
        GenericDAO<Account> accountGenericDAO = new GenericDAO<>(Account.class , primaryDatabase.getEntityManager());
        accountGenericDAO.insert(new Account(
                "test" , "test" , "176" , AccountStatus.LAST_SEEN_RECENTLY , "" , ""
        ));
//
        Console.print("Accounts in primary database: " , Console.Color.GREEN);
        List<Account> accounts = accountGenericDAO.findAll();
        for (Account account : accounts) {
            Console.print(account.toString());
        }

        // Client Usage
        GenericDAO<Account> accountLocalDAO = new GenericDAO<>(Account.class , localDatabase.getEntityManager());
//        accountGenericDAO.insert(new Account(
//                "test" , "test" , "5623246541231" , AccountStatus.LAST_SEEN_RECENTLY , "" , ""
//        ));
        Console.print("Accounts in local database: " , Console.Color.GREEN);

        List<Account> localAccounts = accountLocalDAO.findAll();
        for (Account account : localAccounts) {
            Console.print(account.toString());
        }
    }


}
