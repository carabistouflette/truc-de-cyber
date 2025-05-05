package main.java.com.educational.poc.LoginCloner.parse;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Parses CSS content using regular expressions to find and manipulate resource URLs.
 */
public class RegexCssParser implements ICssParser {

    // Regex to find url() references in CSS, avoiding data URIs and empty urls.
    // Handles optional quotes and whitespace.
    private static final Pattern CSS_URL_PATTERN = Pattern.compile("url\\(\\s*['\"]?([^'\"\\)\\s]+?)['\"]?\\s*\\)");

    @Override
    public List<String> findResourceUrls(String cssContent) {
        List<String> urls = new ArrayList<>();
        if (cssContent == null || cssContent.isEmpty()) {
            return urls;
        }

        Matcher matcher = CSS_URL_PATTERN.matcher(cssContent);
        while (matcher.find()) {
            String urlRef = matcher.group(1).trim();
            // Exclude data URIs, empty strings, and fragments
            if (!urlRef.isEmpty() && !urlRef.startsWith("data:") && !urlRef.startsWith("#")) {
                urls.add(urlRef);
            }
        }
        return urls;
    }

    @Override
    public String replaceUrl(String cssContent, String oldUrl, String newUrl) {
        if (cssContent == null || oldUrl == null || newUrl == null) {
            return cssContent; // Return original content if any input is null
        }

        // We need to match the specific oldUrl within a url() declaration.
        // Escape the oldUrl for use in regex and handle potential surrounding quotes/whitespace.
        // This is tricky because the original regex finds *any* URL. We need to replace a *specific* one.

        // Simpler approach: Iterate through matches and replace if the captured group matches oldUrl.
        Matcher matcher = CSS_URL_PATTERN.matcher(cssContent);
        StringBuffer updatedCss = new StringBuffer();

        while (matcher.find()) {
            String foundUrl = matcher.group(1).trim();
            if (foundUrl.equals(oldUrl)) {
                // Construct the replacement string, keeping the url() structure. Add quotes for safety.
                String replacement = "url('" + Matcher.quoteReplacement(newUrl) + "')";
                matcher.appendReplacement(updatedCss, replacement);
                 System.out.println("      Replaced CSS URL: '" + oldUrl + "' -> '" + newUrl + "'");
            } else {
                // Append the original match if it's not the one we're looking for
                matcher.appendReplacement(updatedCss, Matcher.quoteReplacement(matcher.group(0)));
            }
        }
        matcher.appendTail(updatedCss); // Append the rest of the content

        return updatedCss.toString();

        /* Alternative using String.replace (less precise, might replace unintended occurrences):
           String quotedOldUrl1 = "url('" + oldUrl + "')";
           String quotedOldUrl2 = "url(\"" + oldUrl + "\")";
           String unquotedOldUrl = "url(" + oldUrl + ")";
           // Handle whitespace variations? This gets complex quickly.

           String quotedNewUrl = "url('" + newUrl + "')"; // Standardize replacement

           return cssContent.replace(quotedOldUrl1, quotedNewUrl)
                            .replace(quotedOldUrl2, quotedNewUrl)
                            .replace(unquotedOldUrl, quotedNewUrl); // Less safe
        */
    }
}