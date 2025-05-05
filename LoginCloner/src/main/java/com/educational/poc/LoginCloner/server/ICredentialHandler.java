package main.java.com.educational.poc.LoginCloner.server;

import java.util.Map;

public interface ICredentialHandler {
    void handle(Map<String, String> formData);
}