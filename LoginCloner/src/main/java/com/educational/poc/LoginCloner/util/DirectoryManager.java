package com.educational.poc.LoginCloner.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class DirectoryManager {

    /**
     * Creates the necessary output directories defined in the Configuration.
     * @param cssDir Path to CSS directory.
     * @param imgDir Path to Image directory.
     * @param otherDir Path to Other assets directory.
     * @param scriptDir Path to Script directory.
     * @throws IOException If an error occurs during directory creation.
     */
    public static void createOutputDirectories(Path cssDir, Path imgDir, Path otherDir, Path scriptDir) throws IOException {
        System.out.println("Creating asset directories...");
        Files.createDirectories(cssDir);
        Files.createDirectories(imgDir);
        Files.createDirectories(otherDir);
        Files.createDirectories(scriptDir); // Create script dir too
        System.out.println("Asset directories created/verified at: " + cssDir.getParent().toAbsolutePath());
    }

     /**
     * Creates parent directories for a given file path if they don't exist.
     * @param filePath The path to the file whose parent directories need to be created.
     * @throws IOException If an error occurs during directory creation.
     */
    public static void ensureParentDirectoriesExist(Path filePath) throws IOException {
        Path parentDir = filePath.getParent();
        if (parentDir != null && !Files.exists(parentDir)) {
            Files.createDirectories(parentDir);
        }
    }
}