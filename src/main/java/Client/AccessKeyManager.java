package Client;

import JSocket2.Protocol.Authentication.AuthModel;
import com.google.gson.Gson;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;

public class AccessKeyManager {

    private static final String FILE_PATH = System.getProperty("java.io.tmpdir") + File.separator + "JTelegram" + File.separator + "auth.json";
    private static final Gson GSON = new Gson();

    public static void saveAccessKey(String newAccessKey) {
        AuthModel authModel = loadAuthModel();

        ArrayList<String> keysList;
        if (authModel != null && authModel.getAccessKeys() != null) {
            keysList = new ArrayList<>(Arrays.asList(authModel.getAccessKeys()));
        } else {
            keysList = new ArrayList<>();
        }

        keysList.add(newAccessKey);

        AuthModel updatedAuthModel = new AuthModel(keysList.toArray(new String[0]), keysList.size());

        saveAuthModel(updatedAuthModel);
    }

    public static void deleteAccessKey(String accessKeyToRemove) {
        AuthModel authModel = loadAuthModel();
        if (authModel == null || authModel.getAccessKeys() == null) {
            System.out.println("No keys found to delete.");
            return;
        }

        ArrayList<String> keysList = new ArrayList<>(Arrays.asList(authModel.getAccessKeys()));
        boolean removed = keysList.remove(accessKeyToRemove);

        if (removed) {
            AuthModel updatedModel = new AuthModel(keysList.toArray(new String[0]), keysList.size());
            saveAuthModel(updatedModel);
            System.out.println("Access key successfully deleted.");
        } else {
            System.out.println("The specified access key was not found.");
        }
    }

    public static void clearAllAccessKeys() {
        AuthModel emptyModel = new AuthModel(new String[0], 0);
        saveAuthModel(emptyModel);
        System.out.println("All access keys have been cleared.");
    }

    public static AuthModel loadAuthModel() {
        File authFile = new File(FILE_PATH);

        if (!authFile.exists()) {
            System.out.println("Auth file not found. It will be created on save.");
            return new AuthModel(new String[0], 0);
        }

        try (Reader reader = new FileReader(authFile)) {
            AuthModel loadedModel = GSON.fromJson(reader, AuthModel.class);

            if (loadedModel.getFileHash() != null) {
                AuthModel tempModel = new AuthModel(loadedModel.getAccessKeys(), loadedModel.getAccessKeyCount(), null);
                String currentFileContent = GSON.toJson(tempModel);
                String calculatedHash = calculateHash(currentFileContent);

                if (!calculatedHash.equals(loadedModel.getFileHash())) {
                    System.err.println("ALERT: File integrity check failed! The file may have been tampered with.");
                    return new AuthModel(new String[0], 0);
                }
            } else {
                System.out.println("File has no integrity hash. Cannot verify its authenticity.");
            }

            return loadedModel;

        } catch (IOException e) {
            System.err.println("Failed to load access keys: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private static void saveAuthModel(AuthModel authModel) {
        try {
            Path directory = Paths.get(System.getProperty("java.io.tmpdir"), "JTelegram");
            if (!Files.exists(directory)) {
                Files.createDirectories(directory);
            }

            String modelAsJsonWithoutHash = GSON.toJson(new AuthModel(authModel.getAccessKeys(), authModel.getAccessKeyCount()));
            String hash = calculateHash(modelAsJsonWithoutHash);
            authModel.setFileHash(hash);

            try (Writer writer = new FileWriter(FILE_PATH)) {
                GSON.toJson(authModel, writer);
            }
            System.out.println("Auth model saved with integrity hash.");
        } catch (IOException e) {
            System.err.println("Failed to save auth model: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static String calculateHash(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encodedhash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available.", e);
        }
    }
}
