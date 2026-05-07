package com.bssk;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class FileIntegrityChecker {

    private static final String DATABASE_FILE = "file_states.txt";
    private static final String HASH_ALGORITHM = "SHA-256";

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Podaj sciezke do pliku jako argument.");
            return;
        }

        String filePathInput = args[0];
        Path targetFile = Paths.get(filePathInput);

        if (!Files.exists(targetFile)) {
            System.out.println("Plik nie istnieje.");
            return;
        }

        try {
            FileIntegrityChecker app = new FileIntegrityChecker();
            app.processFileCheck(targetFile);
        } catch (Exception e) {
            System.err.println("Blad krytyczny: " + e.getMessage());
        }
    }

    private void processFileCheck(Path targetFile) throws Exception {
        String currentHash = calculateFileHash(targetFile);
        String absolutePath = targetFile.toAbsolutePath().toString();
        Map<String, String> database = loadDatabase();

        if (!database.containsKey(absolutePath)) {
            database.put(absolutePath, currentHash);
            saveDatabase(database);
            System.out.println("Zapisano nowy stan pliku");
            return;
        }

        String previousHash = database.get(absolutePath);
        if (currentHash.equals(previousHash)) {
            System.out.println("nie zmienilo sie");
        } else {
            database.put(absolutePath, currentHash);
            saveDatabase(database);
            System.out.println("zmienilo sie");
        }
    }

    private String calculateFileHash(Path path) throws NoSuchAlgorithmException, IOException {
        MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
        try (InputStream inputStream = Files.newInputStream(path)) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                digest.update(buffer, 0, bytesRead);
            }
        }
        StringBuilder result = new StringBuilder();
        for (byte b : digest.digest()) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }

    private Map<String, String> loadDatabase() throws IOException {
        Map<String, String> database = new HashMap<>();
        Path dbPath = Paths.get(DATABASE_FILE);
        if (!Files.exists(dbPath)) {
            return database;
        }
        for (String line : Files.readAllLines(dbPath)) {
            String[] parts = line.split("\\|", 2);
            if (parts.length == 2) {
                database.put(parts[0], parts[1]);
            }
        }
        return database;
    }

    private void saveDatabase(Map<String, String> database) throws IOException {
        StringBuilder content = new StringBuilder();
        for (Map.Entry<String, String> entry : database.entrySet()) {
            content.append(entry.getKey()).append("|").append(entry.getValue()).append("\n");
        }
        Files.write(Paths.get(DATABASE_FILE), content.toString().getBytes());
    }
}
