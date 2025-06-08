package Shared.Database;


import Shared.Database.DAO.DAOManager;
import Shared.Utils.Console;

public class DatabaseInitializer {
    public static void  init(){
        Console.clear();
        Database db = new Database();
//        DAOManager.initialize(db.getEntityManager());
        Console.clear();
    }


}
