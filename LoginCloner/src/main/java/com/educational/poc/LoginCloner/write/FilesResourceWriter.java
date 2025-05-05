package com.educational.poc.LoginCloner.write;

import com.educational.poc.LoginCloner.util.DirectoryManager; // Import utility

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Writes resource content (InputStream or String) to files using java.nio.file.Files.
 */
public class FilesResourceWriter implements IResourceWriter {

    @Override
    public boolean write(InputStream contentStream, Path targetPath) throws IOException {
        if (contentStream == null) {
            System.err.println("Cannot write resource: InputStream is null.");
            return false;
        }
        if (targetPath == null) {
            System.err.println("Cannot write resource: Target path is null.");
            return false;
        }

        try {
            DirectoryManager.ensureParentDirectoriesExist(targetPath);
            // System.out.println("      Writing resource stream to: " + targetPath); // Debug log
            Files.copy(contentStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
            // System.out.println("      Successfully wrote resource stream to " + targetPath.getFileName()); // Debug log
            return true;
        } catch (IOException e) {
            System.err.println("      Error saving downloaded file to " + targetPath + ": " + e.getMessage());
            throw e; // Re-throw exception
        } finally {
            // Ensure the input stream is closed by the caller (the orchestrator or downloader wrapper)
            // If the stream came directly from UrlConnectionDownloader, closing it here might be okay,
            // but it's safer to let the component that opened the stream close it.
             try { contentStream.close(); } catch (IOException ce) { /* Ignore close exception */ }
        }
    }

    @Override
    public boolean writeString(String content, Path targetPath) throws IOException {
         if (content == null) {
            System.err.println("Cannot write resource: String content is null.");
            return false;
        }
        if (targetPath == null) {
            System.err.println("Cannot write resource: Target path is null.");
            return false;
        }

        try {
            DirectoryManager.ensureParentDirectoriesExist(targetPath);
            // System.out.println("      Writing resource string to: " + targetPath); // Debug log
            Files.writeString(targetPath, content, StandardCharsets.UTF_8); // Assume UTF-8 for strings
            // System.out.println("      Successfully wrote resource string to " + targetPath.getFileName()); // Debug log
            return true;
        } catch (IOException e) {
            System.err.println("      Error saving string content to " + targetPath + ": " + e.getMessage());
            throw e; // Re-throw exception
        }
    }
}