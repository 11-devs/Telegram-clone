package Shared.Utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileUtil {

    public static void ensureDataDirectoryExists(String dir) {
        try {
            Path path = Paths.get(dir);
            Files.createDirectories(path);
            if (!Files.isWritable(path)) {
                throw new IOException("Directory is not writable: " + dir);
            }
            System.out.println("Ensured directory exists and is writable: " + dir);
        } catch (IOException e) {
            System.err.println("Error creating directory: " + dir + " - " + e.getMessage());
            e.printStackTrace();
            AlertUtil.showError("Failed to create directory: " + dir + " - " + e.getMessage());
            throw new IllegalStateException("Cannot create directory: " + dir, e);
        }
    }
}
