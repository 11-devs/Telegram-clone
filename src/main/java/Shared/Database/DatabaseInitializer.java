package Shared.Database;


import Shared.Database.DAO.GenericDAO;
import Shared.Models.Account.Account;
import Shared.Utils.Console;

import javax.xml.crypto.Data;
import java.util.List;

public class DatabaseInitializer {
    public static void  init(){
        Console.clear();
        Database primaryDatabase = new Database();
        //SQLiteDatabase localDatabase = new SQLiteDatabase();
        Console.print("Database initialized" , Console.Color.GREEN);

//        primaryDatabase.clearAllTables();

        GenericDAO<Account> accountGenericDAO = new GenericDAO<>(Account.class , primaryDatabase.getEntityManager());



        primaryDatabase.printAllTableNames();

//        List<Account> accounts = accountGenericDAO.findAll();
//        for (Account account : accounts) {
//            Console.print("account: " , Console.Color.BLUE);
//            Console.print(account.toString(), Console.Color.BLUE);
//        }
    }

    public static void main(String[] args) {
        init();
    }
}