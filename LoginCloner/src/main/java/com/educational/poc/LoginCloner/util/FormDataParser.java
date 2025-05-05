package main.java.com.educational.poc.LoginCloner.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class FormDataParser {

    /**
     * Parses a URL-encoded form data string into a Map.
     *
     * @param formDataString The URL-encoded string (e.g., "user=test&pass=secret").
     * @return A Map containing the parsed key-value pairs.
     * @throws IllegalArgumentException if decoding fails.
     */
    public static Map<String, String> parse(String formDataString) throws IllegalArgumentException {
        Map<String, String> formData = new HashMap<>();
        if (formDataString == null || formDataString.isEmpty()) {
            return formData;
        }

        String[] pairs = formDataString.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            if (idx > 0) { // Ensure there's a key and the '=' is not the first char
                try {
                    String key = URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8.name());
                    String value = URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8.name());
                    formData.put(key, value);
                } catch (UnsupportedEncodingException e) {
                    // Should not happen with UTF-8, but handle defensively
                    throw new IllegalArgumentException("Failed to decode form data pair: " + pair, e);
                }
            } else if (!pair.isEmpty()) {
                // Handle case where a parameter might not have a value (e.g., "flag&user=test")
                try {
                     String key = URLDecoder.decode(pair, StandardCharsets.UTF_8.name());
                     formData.put(key, ""); // Assign empty value
                } catch (UnsupportedEncodingException e) {
                     throw new IllegalArgumentException("Failed to decode form data key: " + pair, e);
                }
            }
        }
        return formData;
    }
}