package Shared.Utils;

import java.io.File;
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

    /**
     * Gets file extension from filename
     */
    public static String getFileExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        return (lastDot == -1) ? "" : fileName.substring(lastDot + 1);
    }

    /**
     * Formats file size in human-readable format
     */
    public static String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "";
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }

    public static String getFileNameWithoutExtension(String sourceFileName) {
        if (sourceFileName == null || sourceFileName.isEmpty()) {
            return sourceFileName;
        }

        // If a full path is provided, extract just the file name part.
        String fileName = new File(sourceFileName).getName();

        int dotIndex = fileName.lastIndexOf('.');

        // A dot index of 0 means it's a dotfile (e.g., ".bashrc").
        // A dot index of -1 means there is no extension.
        // In both cases, the original file name should be returned.
        if (dotIndex > 0) {
            return fileName.substring(0, dotIndex);
        } else {
            return fileName;
        }
    }

}