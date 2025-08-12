package Shared.Database;


import Shared.Utils.Console;

import javax.xml.crypto.Data;
import java.util.List;

public class DatabaseInitializer {
    public static void  init(){
        Console.clear();
        Database primaryDatabase = new Database();
        SQLiteDatabase localDatabase = new SQLiteDatabase();



    }

    public static void main(String[] args) {
        init();
    }


}
