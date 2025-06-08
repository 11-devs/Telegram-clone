package Shared.Utils;
import java.util.Scanner;

public class Console {
    public enum Color {
        // Foreground Colors
        BLACK("\033[30m"),
        RED("\033[31m"),
        GREEN("\033[32m"),
        YELLOW("\033[33m"),
        BLUE("\033[34m"),
        MAGENTA("\033[35m"),
        CYAN("\033[36m"),
        WHITE("\033[37m"),

        // Light (Bright) Foreground Colors
        LIGHT_BLACK("\033[90m"),
        LIGHT_RED("\033[91m"),
        LIGHT_GREEN("\033[92m"),
        LIGHT_YELLOW("\033[93m"),
        LIGHT_BLUE("\033[94m"),
        LIGHT_MAGENTA("\033[95m"),
        LIGHT_CYAN("\033[96m"),
        LIGHT_WHITE("\033[97m"),

        // Background Colors
        BLACK_BACKGROUND("\033[40m"),
        RED_BACKGROUND("\033[41m"),
        GREEN_BACKGROUND("\033[42m"),
        YELLOW_BACKGROUND("\033[43m"),
        BLUE_BACKGROUND("\033[44m"),
        MAGENTA_BACKGROUND("\033[45m"),
        CYAN_BACKGROUND("\033[46m"),
        WHITE_BACKGROUND("\033[47m"),

        // Light (Bright) Background Colors
        LIGHT_BLACK_BACKGROUND("\033[100m"),
        LIGHT_RED_BACKGROUND("\033[101m"),
        LIGHT_GREEN_BACKGROUND("\033[102m"),
        LIGHT_YELLOW_BACKGROUND("\033[103m"),
        LIGHT_BLUE_BACKGROUND("\033[104m"),
        LIGHT_MAGENTA_BACKGROUND("\033[105m"),
        LIGHT_CYAN_BACKGROUND("\033[106m"),
        LIGHT_WHITE_BACKGROUND("\033[107m"),

        // Text Styles (Bold, Underline, Italics, etc.)
        BOLD("\033[1m"),
        ITALIC("\033[3m"),
        UNDERLINE("\033[4m"),
        RESET("\033[0m");  // RESET for resetting to default color

        private final String colorCode;

        Color(String colorCode) {
            this.colorCode = colorCode;
        }

        public String getColorCode() {
            return this.colorCode;
        }

        public String applyColor(String text) {
            return this.colorCode + text + RESET.getColorCode();
        }
    }

    // Print method with specified color and paddingEnabled
    public static void print(String msg, Console.Color color, Boolean paddingEnabled) {
        String padding = "";
        if (paddingEnabled != null && paddingEnabled) {
            padding = "    "; // Default padding
        }
        System.out.print(color.getColorCode() + padding + msg + "\n" + Console.Color.RESET.getColorCode());
    }

    // Print method with default paddingEnabled set to true
    public static void print(String msg, Console.Color color) {
        print(msg, color, true); // Default paddingEnabled to true
    }

    // Print method with default paddingEnabled set to true and default color to WHITE
    public static void print(String msg) {
        print(msg, Console.Color.WHITE, true); // Default color WHITE and paddingEnabled true
    }

    public static void printError(String msg) {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();

        // stackTrace[0] = getStackTrace
        // stackTrace[1] = printError
        // stackTrace[2] = the method that called printError
        if (stackTrace.length > 2) {
            StackTraceElement caller = stackTrace[2];
            String className = caller.getClassName();
            String methodName = caller.getMethodName();
            int lineNumber = caller.getLineNumber();
            msg = "[" + className + "." + methodName + "():" + lineNumber + "] " + msg;
        }

        print("Error: " + msg, Console.Color.RED, true);
    }

    public static void error(String msg) {
        printError(msg);
    }

    public static void error(Exception e) {
        printError(e.getMessage());
    }

    public static void log(String msg) {
        print(msg , Color.LIGHT_BLUE);
    }


    public static void clear() {
        try {
            new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
        } catch (Exception e) {
            Console.print("____________________________________________________" , Color.LIGHT_BLUE_BACKGROUND);
            System.out.println("Error clearing console: " + e.getMessage());
            Console.print("____________________________________________________" , Color.LIGHT_BLUE_BACKGROUND);

        }
    }

    public static void commandLabel(String label , int index){
        Console.print( String.valueOf(index) + " - " , Console.Color.RED);
        Console.print( label + "\n" , Console.Color.WHITE , false);
    }

    public static String getInput() {
        return Console.getInput("");
    }

    public static String getInput(String prompt) {
        Scanner scanner = new Scanner(System.in);
        Console.print("\n   " + prompt , Console.Color.CYAN);
        return scanner.nextLine();
    }

    public static String getInputBox(String header, String[] commands, String prompt) {

        Console.print(header + "\n\n", Console.Color.YELLOW);
        int i = 1;
        for (String command : commands) {
            Console.commandLabel(command, i++);
        }
        Console.print("\n");

        String input;
        int choice = -1;
        while (true) {
            input = Console.getInput(prompt);

            try {
                choice = Integer.parseInt(input);
                if (choice >= 1 && choice <= commands.length) {
                    break; // Valid input
                } else {
                    Console.print("\n   Please enter a number between 1 and " + commands.length + ".", Console.Color.RED);
                }
            } catch (NumberFormatException e) {
                Console.print("\n   Invalid input. Please enter a number.", Console.Color.RED);
            }
        }

        return String.valueOf(choice);
    }
}