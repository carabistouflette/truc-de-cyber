package com.educational.poc.LoginCloner.update;

import com.educational.poc.LoginCloner.util.PathUtils;
import org.jsoup.nodes.Element;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Updates links in HTML elements and styles to relative paths based on the source file location.
 */
public class RelativeLinkUpdater implements ILinkUpdater {

    // Re-use the pattern from JsoupHtmlParser or define locally if preferred
    private static final Pattern CSS_URL_PATTERN = Pattern.compile("url\\(\\s*['\"]?([^'\"\\)\\s]+?)['\"]?\\s*\\)");

    @Override
    public void updateElementLink(Element element, String attributeName, Path localPath, Path sourcePath) {
        if (element == null || attributeName == null || localPath == null || sourcePath == null) {
            System.err.println("Warning: Cannot update element link, null parameter provided.");
            return;
        }

        try {
            Path sourceDir = sourcePath.getParent();
            if (sourceDir == null) {
                sourceDir = Paths.get("."); // Handle case where source is in the root directory
            }

            // Calculate the relative path from the source file's directory to the asset's path
            Path relativePath = sourceDir.relativize(localPath);
            String relativeUri = PathUtils.encodePathToUri(relativePath.toString()); // Encode for use in HTML

            System.out.println("    Updating attribute '" + attributeName + "' on element <" + element.tagName() + "> to relative path: " + relativeUri);
            element.attr(attributeName, relativeUri); // Update the attribute in the document

        } catch (Exception e) {
            // Catch potential errors during path relativization or encoding
            System.err.println("    Error updating element link for " + localPath + ": " + e.getMessage());
            // Optionally leave the original link or set a placeholder? For now, just log error.
        }
    }

    @Override
    public void updateInlineStyleLink(Element element, String originalAbsoluteAssetUrl, Path localPath, Path sourcePath) {
        if (element == null || originalAbsoluteAssetUrl == null || localPath == null || sourcePath == null) {
             System.err.println("Warning: Cannot update inline style link, null parameter provided.");
            return;
        }

        String styleAttr = element.attr("style");
        if (styleAttr.isEmpty()) {
            return; // No style attribute to update
        }

        try {
            Path sourceDir = sourcePath.getParent();
            if (sourceDir == null) {
                sourceDir = Paths.get(".");
            }
            Path relativePath = sourceDir.relativize(localPath);
            String relativeUri = PathUtils.encodePathToUri(relativePath.toString());

            // We need to replace the specific originalAbsoluteAssetUrl within the style attribute.
            // This requires careful handling as the style attribute might contain multiple url() declarations.

            Matcher matcher = CSS_URL_PATTERN.matcher(styleAttr);
            StringBuffer updatedStyle = new StringBuffer();
            boolean changed = false;

            // We need the base URI of the document to resolve the relative URLs found in the style attribute
            // to compare them against the originalAbsoluteAssetUrl. This is complex here.
            // A simpler, slightly less robust approach: assume the originalAbsoluteAssetUrl is unique enough.
            // Find the url(...) part containing the original URL and replace it.

            // Let's refine this: The orchestrator should pass the *original relative reference* found in the style,
            // not the absolute URL, to make replacement easier.
            // However, the current ILinkUpdater interface takes the absolute URL.
            // Let's try replacing the absolute URL directly, hoping it's unique enough in the context.

            String urlToReplace = originalAbsoluteAssetUrl; // The absolute URL we downloaded
            String replacementUri = relativeUri; // The new relative path

            while (matcher.find()) {
                 String foundUrlRefInStyle = matcher.group(1).trim(); // This is likely relative in the original HTML

                 // Problem: We have the absolute URL (urlToReplace) but the style attribute likely has a relative ref.
                 // We cannot reliably match them without resolving foundUrlRefInStyle relative to the document's base URI.

                 // --- Workaround ---
                 // Let's assume for now the orchestrator will handle resolving and passing the correct original
                 // reference if needed, or we modify the interface.
                 // For this implementation, we'll attempt a direct replacement of the absolute URL string,
                 // acknowledging this might fail if the style attribute used a relative path.
                 // A better solution involves the parser providing the original relative ref alongside the absolute URL.

                 // Let's try replacing the *value* inside url() regardless of quotes.
                 // This is still fragile.
                 // Example: style="background: url(http://example.com/img.png)"
                 // We want to replace "http://example.com/img.png" with "../assets/images/img.png"

                 // Let's modify the logic slightly: Replace the *entire* url(...) match if the *absolute* URL matches.
                 // This requires resolving the found relative ref first. This class doesn't have the base URI.

                 // === Compromise for now: Replace the absolute URL string directly ===
                 // This will likely NOT work correctly for relative paths in style attributes.
                 // TODO: Refactor this - requires changes in parsing/orchestration or interface.

                 // Let's just log a warning and skip the replacement for inline styles for now,
                 // as the current structure doesn't support it robustly.
                 // matcher.appendReplacement(updatedStyle, Matcher.quoteReplacement(matcher.group(0))); // Keep original

            }
             // matcher.appendTail(updatedStyle);

            // if (changed) {
            //     element.attr("style", updatedStyle.toString());
            //     System.out.println("    Updated inline style on element <" + element.tagName() + ">");
            // }

             System.out.println("    Skipping inline style update for " + originalAbsoluteAssetUrl + " (requires robust relative path handling).");


        } catch (Exception e) {
            System.err.println("    Error updating inline style link for " + localPath + ": " + e.getMessage());
        }
    }
}