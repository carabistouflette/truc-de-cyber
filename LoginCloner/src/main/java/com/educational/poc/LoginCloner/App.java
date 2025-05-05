package com.educational.poc.LoginCloner;

import main.java.com.educational.poc.LoginCloner.core.Configuration;
import main.java.com.educational.poc.LoginCloner.core.LoginClonerOrchestrator;
import main.java.com.educational.poc.LoginCloner.detect.AdvancedFormDetector;
import main.java.com.educational.poc.LoginCloner.download.UrlConnectionDownloader;
import main.java.com.educational.poc.LoginCloner.fetch.JsoupFetcher;
import main.java.com.educational.poc.LoginCloner.parse.JsoupHtmlParser;
import main.java.com.educational.poc.LoginCloner.parse.RegexCssParser;
import main.java.com.educational.poc.LoginCloner.server.ConsoleCredentialHandler;
import main.java.com.educational.poc.LoginCloner.server.SimpleHttpServer;
import main.java.com.educational.poc.LoginCloner.update.RelativeLinkUpdater;
import main.java.com.educational.poc.LoginCloner.write.FileWriterDocumentWriter;
import main.java.com.educational.poc.LoginCloner.write.FilesResourceWriter;

/**
 * Entry point for the LoginCloner application.
 * Sets up dependencies and delegates execution to the LoginClonerOrchestrator.
 */
public class App {

    public static void main(String[] args) {
        // Check for correct number of arguments
        if (args.length != 1) {
            System.out.println("Usage: java -cp <classpath> main.java.com.educational.poc.LoginCloner.App <URL>");
            // Note: Adjust classpath based on build system (e.g., Maven target/classes)
            return; // Exit if incorrect usage
        }

        String url = args[0];

        // --- Dependency Injection (Manual) ---
        // Instantiate concrete implementations
        JsoupFetcher fetcher = new JsoupFetcher();
        JsoupHtmlParser htmlParser = new JsoupHtmlParser();
        RegexCssParser cssParser = new RegexCssParser();
        UrlConnectionDownloader downloader = new UrlConnectionDownloader();
        FilesResourceWriter resourceWriter = new FilesResourceWriter(); // Using Files API for resources
        RelativeLinkUpdater linkUpdater = new RelativeLinkUpdater();
        AdvancedFormDetector formDetector = new AdvancedFormDetector();
        FileWriterDocumentWriter documentWriter = new FileWriterDocumentWriter(); // Using FileWriter for HTML doc
        ConsoleCredentialHandler credentialHandler = new ConsoleCredentialHandler();
        SimpleHttpServer server = new SimpleHttpServer(
                Configuration.SERVER_PORT,
                Configuration.LOGIN_HANDLER_PATH,
                credentialHandler
        );

        // Instantiate the orchestrator with dependencies
        LoginClonerOrchestrator orchestrator = new LoginClonerOrchestrator(
                fetcher,
                htmlParser,
                cssParser,
                downloader,
                resourceWriter,
                linkUpdater,
                formDetector,
                documentWriter,
                server
        );

        // --- Run the Application ---
        System.out.println("Starting LoginCloner for URL: " + url);
        orchestrator.run(url);
        // The server runs until explicitly stopped (e.g., Ctrl+C or shutdown hook)
        // System.out.println("LoginCloner process finished (Server might still be running).");
    }
}
