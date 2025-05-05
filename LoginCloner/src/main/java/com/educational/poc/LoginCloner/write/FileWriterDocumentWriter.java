package main.java.com.educational.poc.LoginCloner.write;

import main.java.com.educational.poc.LoginCloner.util.DirectoryManager; // Import utility
import org.jsoup.nodes.Document;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Writes a Jsoup Document object to a file using FileWriter.
 */
public class FileWriterDocumentWriter implements IDocumentWriter {

    /**
     * Saves the given Jsoup Document object to a file at the specified path.
     * Ensures parent directories exist before writing.
     *
     * @param doc      The Document to save. Should not be null.
     * @param filePath The path (relative or absolute) where the file should be saved. Should not be null.
     * @return true if the file was saved successfully, false otherwise.
     * @throws IOException if an error occurs during directory creation or file writing.
     */
    @Override
    public boolean write(Document doc, Path filePath) throws IOException {
        if (doc == null) {
            System.err.println("Cannot save document: Document object is null.");
            return false;
        }
        if (filePath == null) {
            System.err.println("Cannot save document: File path is null.");
            return false;
        }

        // Ensure parent directories exist
        try {
            DirectoryManager.ensureParentDirectoriesExist(filePath);
        } catch (IOException e) {
            System.err.println("Error creating parent directories for '" + filePath + "': " + e.getMessage());
            throw e; // Re-throw exception as we cannot proceed
        }

        String htmlContent = doc.outerHtml(); // Get the complete HTML of the modified document
        System.out.println("Attempting to write document to: " + filePath.toAbsolutePath());
        try (FileWriter writer = new FileWriter(filePath.toFile())) { // Use Path.toFile()
            writer.write(htmlContent);
            System.out.println("Successfully wrote " + htmlContent.length() + " characters to " + filePath.toAbsolutePath());
            return true; // Indicate success
        } catch (IOException e) {
            System.err.println("Error writing document to file '" + filePath + "': " + e.getMessage());
            // e.printStackTrace(); // Optional: print stack trace for detailed debugging
            throw e; // Re-throw exception
        }
    }
}