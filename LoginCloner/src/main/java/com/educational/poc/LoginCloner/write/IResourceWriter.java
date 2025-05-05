package com.educational.poc.LoginCloner.write;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

public interface IResourceWriter {
    /**
     * Writes content from an InputStream to a file.
     * Creates parent directories if they don't exist.
     * Replaces the file if it already exists.
     *
     * @param contentStream The InputStream containing the resource content.
     * @param targetPath    The path where the file should be saved.
     * @return true if writing was successful, false otherwise.
     * @throws IOException if an I/O error occurs during writing or directory creation.
     */
    boolean write(InputStream contentStream, Path targetPath) throws IOException;

     /**
     * Writes string content to a file.
     * Creates parent directories if they don't exist.
     * Replaces the file if it already exists.
     *
     * @param content    The String content to write.
     * @param targetPath The path where the file should be saved.
     * @return true if writing was successful, false otherwise.
     * @throws IOException if an I/O error occurs during writing or directory creation.
     */
    boolean writeString(String content, Path targetPath) throws IOException;
}