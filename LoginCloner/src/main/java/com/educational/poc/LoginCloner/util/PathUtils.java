package com.educational.poc.LoginCloner.util;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files; // Added Files import
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

public class PathUtils {

    /**
     * Extracts the file extension from a filename or URL path.
     * Includes the leading dot. Returns empty string if no extension found.
     * Handles query parameters and fragments.
     */
    public static String getExtension(String fileNameOrUrlPath) {
         String pathPart = fileNameOrUrlPath;
         // Remove query string and fragment
         int queryIndex = pathPart.indexOf('?');
         if (queryIndex != -1) {
             pathPart = pathPart.substring(0, queryIndex);
         }
         int fragmentIndex = pathPart.indexOf('#');
         if (fragmentIndex != -1) {
             pathPart = pathPart.substring(0, fragmentIndex);
         }

        int lastDot = pathPart.lastIndexOf('.');
        int lastSlash = pathPart.lastIndexOf('/'); // Ensure dot is part of filename, not directory
        if (lastDot > lastSlash) { // Check if dot is after the last slash
            return pathPart.substring(lastDot);
        }
        return ""; // No extension found in the filename part
    }

    /**
     * Encodes a file path string into a valid URI path component,
     * handling spaces and other special characters. Replaces backslashes with forward slashes.
     */
     public static String encodePathToUri(String path) {
         try {
             // Replace backslashes FIRST
             String forwardSlashPath = path.replace('\\', '/');
             // Basic encoding for relative paths - encode spaces, etc.
             // Using URI constructor for path encoding might be too aggressive for relative paths.
             // Manual replacement of common problematic chars for href/src:
             return forwardSlashPath.replace(" ", "%20")
                                    .replace("#", "%23")
                                    .replace("?", "%3F");
             // For more complex cases, a dedicated URL encoding library might be needed.
             // Example using URI (might over-encode):
             // return new URI(null, null, forwardSlashPath, null).toASCIIString();
         } catch (Exception e) { // Catch broader exceptions just in case
              System.err.println("    Warning: Could not properly encode path to URI: " + path + " - " + e.getMessage());
              // Fallback: replace backslashes and spaces (common issue)
              return path.replace('\\', '/').replace(" ", "%20");
         }
     }

    /**
     * Sanitizes a filename derived from a URL path.
     * Replaces non-alphanumeric characters (excluding ., -, _) with underscores.
     * Generates a unique name if the result is empty, just an underscore, or lacks an extension.
     */
    public static String sanitizeFileName(String urlPath) {
        String originalFileName = Paths.get(urlPath).getFileName().toString();
        // Remove query parameters/fragments first if they exist
        int queryIndex = originalFileName.indexOf('?');
        if (queryIndex != -1) originalFileName = originalFileName.substring(0, queryIndex);
        int fragmentIndex = originalFileName.indexOf('#');
        if (fragmentIndex != -1) originalFileName = originalFileName.substring(0, fragmentIndex);

        String sanitized = originalFileName.replaceAll("[^a-zA-Z0-9.\\-_]", "_");

        if (sanitized.isEmpty() || sanitized.equals("_") || sanitized.lastIndexOf('.') <= 0) { // Check if extension is missing or at start
            // Keep original extension if possible, otherwise generate random name + extension
            String extension = getExtension(urlPath); // Use original path to get extension
            return "resource_" + UUID.randomUUID().toString().substring(0, 8) + (extension.isEmpty() ? ".unknown" : extension);
        }
        return sanitized;
    }

     /**
      * Creates a unique local path by appending a counter if the file already exists.
      * @param targetDir The directory where the file should be saved.
      * @param desiredFileName The initially desired (sanitized) filename.
      * @return A Path object representing a unique filename within the target directory.
      */
     public static Path getUniqueLocalPath(Path targetDir, String desiredFileName) {
         Path localPath = targetDir.resolve(desiredFileName);
         if (!Files.exists(localPath)) {
             return localPath; // Path is already unique
         }

         String baseName = desiredFileName;
         String extension = "";
         int lastDot = desiredFileName.lastIndexOf('.');
         if (lastDot > 0) { // Ensure dot is not the first character
              baseName = desiredFileName.substring(0, lastDot);
              extension = desiredFileName.substring(lastDot);
         } else {
              // Handle case with no extension or dot at the beginning
              baseName = desiredFileName;
              extension = "";
         }

         int counter = 1;
         while (Files.exists(localPath)) {
              String newName = baseName + "_" + counter + extension;
              localPath = targetDir.resolve(newName);
              counter++;
              if (counter > 1000) { // Safety break
                  System.err.println("Warning: Could not find unique filename after 1000 attempts for: " + desiredFileName);
                  // Return a random name as a last resort
                  return targetDir.resolve(baseName + "_" + UUID.randomUUID().toString().substring(0, 8) + extension);
              }
         }
         return localPath;
     }
}