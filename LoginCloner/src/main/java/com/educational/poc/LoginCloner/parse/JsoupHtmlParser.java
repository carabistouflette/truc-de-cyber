package com.educational.poc.LoginCloner.parse;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses HTML documents using Jsoup to find resource links.
 */
public class JsoupHtmlParser implements IHtmlParser {

    // Regex to find url() references in CSS, avoiding data URIs and empty urls.
    private static final Pattern CSS_URL_PATTERN = Pattern.compile("url\\(\\s*['\"]?([^'\"\\)\\s]+?)['\"]?\\s*\\)");

    @Override
    public List<ResourceInfo> findExternalResources(Document doc) {
        List<ResourceInfo> resources = new ArrayList<>();
        if (doc == null) {
            return resources;
        }

        System.out.println("Parsing HTML for external resources (CSS, Images, Scripts)...");

        // Process CSS Links
        Elements cssLinks = doc.select("link[rel=stylesheet][href]");
        for (Element link : cssLinks) {
            addResourceInfo(resources, link, "href", ResourceType.CSS_LINK);
        }

        // Process Image Sources
        Elements imgElements = doc.select("img[src]");
        for (Element img : imgElements) {
            addResourceInfo(resources, img, "src", ResourceType.IMAGE);
        }

        // Process Script Sources (Optional - can be enabled/disabled by orchestrator)
        // Elements scriptElements = doc.select("script[src]");
        // for (Element script : scriptElements) {
        //     addResourceInfo(resources, script, "src", ResourceType.SCRIPT);
        // }

        System.out.println("Found " + resources.size() + " external resource links.");
        return resources;
    }

    @Override
    public Map<Element, List<String>> findInlineStyleResources(Document doc) {
        Map<Element, List<String>> styleResources = new HashMap<>();
         if (doc == null) {
            return styleResources;
        }
        String baseUri = doc.baseUri(); // Use document's base URI for resolving relative paths

        System.out.println("Parsing HTML for inline style resources...");
        Elements elementsWithStyle = doc.select("[style]");

        for (Element styledElement : elementsWithStyle) {
            String styleAttr = styledElement.attr("style");
            Matcher matcher = CSS_URL_PATTERN.matcher(styleAttr);
            List<String> elementUrls = new ArrayList<>();

            while (matcher.find()) {
                String originalUrlRef = matcher.group(1).trim(); // The URL inside url()

                if (originalUrlRef.isEmpty() || originalUrlRef.startsWith("data:") || originalUrlRef.startsWith("#")) {
                    continue; // Skip data URIs, empty refs, or fragments
                }

                try {
                    // Resolve the asset URL relative to the document's base URI
                    URI resolvedUri = new URI(baseUri).resolve(originalUrlRef);
                    String absoluteAssetUrlStr = resolvedUri.toString();
                    elementUrls.add(absoluteAssetUrlStr);
                     System.out.println("  Found inline style asset: " + originalUrlRef + " -> " + absoluteAssetUrlStr);
                } catch (URISyntaxException | IllegalArgumentException e) {
                    System.err.println("  Skipping invalid asset URL in inline style: " + originalUrlRef + " (relative to " + baseUri + ") - " + e.getMessage());
                }
            }
            if (!elementUrls.isEmpty()) {
                styleResources.put(styledElement, elementUrls);
            }
        }
         System.out.println("Found inline style resources in " + styleResources.size() + " elements.");
        return styleResources;
    }


    /**
     * Helper to extract absolute URL and add ResourceInfo to the list.
     */
    private void addResourceInfo(List<ResourceInfo> resources, Element element, String attributeName, ResourceType type) {
        String resourceUrlStr = element.attr("abs:" + attributeName); // Get absolute URL directly

        if (resourceUrlStr != null && !resourceUrlStr.isEmpty() && !resourceUrlStr.startsWith("data:")) {
            resources.add(new ResourceInfo(resourceUrlStr, element, attributeName, type));
            // System.out.println("  Found " + type + ": " + resourceUrlStr); // Debug log
        } else {
            // System.out.println("  Skipping " + type + " (empty, data URI, or relative failed): " + element.attr(attributeName)); // Debug log
        }
    }
}