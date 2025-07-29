package Shared.Database;


import Shared.Database.Repository.RepositoryManager;
import Shared.Models.Account.Account;
import Shared.Utils.Console;

import javax.xml.crypto.Data;
import java.util.List;

public class DatabaseInitializer {
    public static void  init(){
        Console.clear();
        Database primaryDatabase = new Database();
        SQLiteDatabase localDatabase = new SQLiteDatabase();
        RepositoryManager.initialize(primaryDatabase.getEntityManager() , localDatabase.getEntityManager());


        List<Account> accounts =  RepositoryManager.accountRepository.findAll();
        Console.print(accounts.size() + " accounts");
        for (Account account : accounts) {
            Console.print("One Account found!");
            Console.print(account.toString());

        }

//        Database db = new Database();
//        DAOManager.initialize(db.getEntityManager());
        Console.clear();


    }

    public static void main(String[] args) {
        init();
    }


}
