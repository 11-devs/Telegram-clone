package JSocket2.Utils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Logger {

    public enum LogLevel {
        INFO, WARN, ERROR, DEBUG
    }

    private static Logger instance;
    private boolean debugEnabled = true;
    private BufferedWriter writer;
    private static final String LOG_FILE_PATH = "logs/app.log";

    private Logger() {
        try {

            File logDir = new File("logs");
            if (!logDir.exists()) {
                logDir.mkdirs();
            }

            writer = new BufferedWriter(new FileWriter(LOG_FILE_PATH, true));
        } catch (IOException e) {
            System.err.println("Logger initialization failed: " + e.getMessage());
        }
    }

    public static Logger get() {
        if (instance == null) {
            instance = new Logger();
        }
        return instance;
    }

    public void info(String message) {
        log(LogLevel.INFO, message);
    }

    public void warn(String message) {
        log(LogLevel.WARN, message);
    }

    public void error(String message) {
        log(LogLevel.ERROR, message);
    }

    public void debug(String message) {
        if (debugEnabled) {
            log(LogLevel.DEBUG, message);
        }
    }

    private void log(LogLevel level, String message) {
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String logEntry = "[" + time + "] [" + level + "] " + message;


        System.out.println(logEntry);


        if (writer != null) {
            try {
                writer.write(logEntry);
                writer.newLine();
                writer.flush();
            } catch (IOException e) {
                System.err.println("Logger write failed: " + e.getMessage());
            }
        }
    }

    public void setDebugEnabled(boolean enabled) {
        this.debugEnabled = enabled;
    }

    public void close() {
        try {
            if (writer != null) writer.close();
        } catch (IOException e) {
            System.err.println("Logger close failed: " + e.getMessage());
        }
    }
}
