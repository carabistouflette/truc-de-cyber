package com.educational.poc.LoginCloner.download;

import com.educational.poc.LoginCloner.core.Configuration; // Import configuration

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Downloads resources using Java's HttpURLConnection.
 */
public class UrlConnectionDownloader implements IDownloader {

    private static final int MAX_REDIRECTS = 5;

    @Override
    public InputStream download(String resourceUrlStr) throws IOException {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(resourceUrlStr);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(Configuration.CONNECT_TIMEOUT_MS); // Use config
            connection.setReadTimeout(Configuration.READ_TIMEOUT_MS);       // Use config
            connection.setRequestProperty("User-Agent", Configuration.USER_AGENT); // Use config
            connection.setInstanceFollowRedirects(true); // Let HttpURLConnection handle redirects initially

            int responseCode = connection.getResponseCode();
            int redirectCount = 0;

            // Handle redirects manually only if needed (e.g., if setInstanceFollowRedirects(false))
            // or to add specific logging/logic during redirects.
            // setInstanceFollowRedirects(true) is generally preferred.
            /*
            while ((responseCode == HttpURLConnection.HTTP_MOVED_TEMP || responseCode == HttpURLConnection.HTTP_MOVED_PERM || responseCode == HttpURLConnection.HTTP_SEE_OTHER)
                   && redirectCount < MAX_REDIRECTS) {
                String newUrl = connection.getHeaderField("Location");
                System.out.println("      Redirecting to: " + newUrl);
                connection.disconnect();
                url = new URL(url, newUrl); // Handle relative redirects
                connection = (HttpURLConnection) url.openConnection();
                // Re-apply settings
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(Configuration.CONNECT_TIMEOUT_MS);
                connection.setReadTimeout(Configuration.READ_TIMEOUT_MS);
                connection.setRequestProperty("User-Agent", Configuration.USER_AGENT);
                connection.setInstanceFollowRedirects(true); // Keep true for subsequent internal redirects
                responseCode = connection.getResponseCode();
                redirectCount++;
            }
            */

            if (responseCode >= 200 && responseCode < 300) { // Check for success codes (2xx)
                // Return the InputStream; the caller is responsible for closing it and the connection.
                // We cannot close the connection here, otherwise the stream becomes unusable.
                System.out.println("      Successfully connected to download: " + connection.getURL());
                return connection.getInputStream();
            } else {
                System.err.println("      HTTP Error " + responseCode + " for URL: " + connection.getURL()); // Show final URL after redirects
                connection.disconnect(); // Disconnect on error
                return null; // Indicate failure
            }
        } catch (IOException e) {
            System.err.println("      Download failed for " + resourceUrlStr + ": " + e.getMessage());
            if (connection != null) {
                connection.disconnect(); // Ensure disconnection on exception
            }
            throw e; // Re-throw exception
        }
        // Note: The HttpURLConnection is intentionally not disconnected on success here.
        // The caller who reads the InputStream is responsible for closing the stream,
        // which should implicitly allow the connection resources to be released.
        // If issues arise, explicitly returning the connection along with the stream might be needed.
    }
}