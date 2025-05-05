package main.java.com.educational.poc.LoginCloner.fetch;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import java.io.IOException;

/**
 * Fetches HTML content from a URL using Jsoup.
 */
public class JsoupFetcher implements IFetcher {

    /**
     * Fetches HTML content from the given URL string using Jsoup.
     *
     * @param urlString The URL to fetch HTML from.
     * @return The fetched Document object.
     * @throws IOException if an error occurs during fetching (e.g., network error, invalid URL).
     * @throws IllegalArgumentException if the urlString is malformed.
     */
    @Override
    public Document fetch(String urlString) throws IOException, IllegalArgumentException {
        System.out.println("Fetching URL: " + urlString);
        try {
            // Jsoup handles basic validation and throws IllegalArgumentException for malformed URLs
            Document doc = Jsoup.connect(urlString).get();
            System.out.println("Successfully fetched page: " + doc.title());
            return doc;
        } catch (IOException | IllegalArgumentException e) {
            System.err.println("Error fetching URL '" + urlString + "': " + e.getMessage());
            throw e; // Re-throw the exception to be handled by the caller
        }
    }
}