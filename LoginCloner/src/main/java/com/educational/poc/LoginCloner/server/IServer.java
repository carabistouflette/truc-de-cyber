package main.java.com.educational.poc.LoginCloner.server;

import java.io.IOException;

public interface IServer {
    void start() throws IOException;
    void stop();
}