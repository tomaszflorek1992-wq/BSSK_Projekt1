package com.bssk;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class FileIntegrityChecker {

    private static final String DATABASE_FILE = "file_states.txt";
    private static final String HASH_ALGORITHM = "SHA-256";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String filePathInput;

        if (args.length >= 1) {
            filePathInput = args[0];
        } else {
            System.out.print("Podaj sciezke do pliku: ");
            filePathInput = scanner.nextLine().trim();
        }

        Path targetFile = Paths.get(filePathInput);

        if (!Files.exists(targetFile)) {
            System.out.println("Plik nie istnieje: " + filePathInput);
            return;
        }

        if (Files.isDirectory(targetFile)) {
            System.out.println("Podana sciezka prowadzi do katalogu, nie do pliku.");
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
        String currentTime = LocalDateTime.now().format(FORMATTER);
        String absolutePath = targetFile.toAbsolutePath().toString();
        Map<String, String[]> database = loadDatabase();

        if (!database.containsKey(absolutePath)) {
            database.put(absolutePath, new String[]{currentHash, currentTime});
            saveDatabase(database);
            System.out.println("Plik nie byl wczesniej analizowany.");
            System.out.println("Zapisano nowy stan pliku o: " + currentTime);
            return;
        }

        String[] previousEntry = database.get(absolutePath);
        String previousHash = previousEntry[0];
        String previousTime = previousEntry[1];

        if (currentHash.equals(previousHash)) {
            System.out.println("Nie zmienilo sie.");
            System.out.println("Ostatnio sprawdzano: " + previousTime);
        } else {
            database.put(absolutePath, new String[]{currentHash, currentTime});
            saveDatabase(database);
            System.out.println("Zmienilo sie!");
            System.out.println("Poprzedni stan zapisany: " + previousTime);
            System.out.println("Nowy stan zapisany o:    " + currentTime);
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

    private Map<String, String[]> loadDatabase() throws IOException {
        Map<String, String[]> database = new HashMap<>();
        Path dbPath = Paths.get(DATABASE_FILE);
        if (!Files.exists(dbPath)) {
            return database;
        }
        for (String line : Files.readAllLines(dbPath)) {
            String[] parts = line.split("\\|", 3);
            if (parts.length == 3) {
                database.put(parts[0], new String[]{parts[1], parts[2]});
            }
        }
        return database;
    }

    private void saveDatabase(Map<String, String[]> database) throws IOException {
        StringBuilder content = new StringBuilder();
        for (Map.Entry<String, String[]> entry : database.entrySet()) {
            content.append(entry.getKey())
                    .append("|")
                    .append(entry.getValue()[0])
                    .append("|")
                    .append(entry.getValue()[1])
                    .append("\n");
        }
        Files.write(Paths.get(DATABASE_FILE), content.toString().getBytes());
    }
}