package com.educational.poc.LoginCloner.parse;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import java.util.List;
import java.util.Map;

public interface IHtmlParser {
    /** Represents information about a resource found in HTML */
    class ResourceInfo {
        public final String url; // Absolute URL
        public final Element element; // The element containing the resource link
        public final String attributeName; // e.g., "href", "src", "style"
        public final ResourceType type;

        public ResourceInfo(String url, Element element, String attributeName, ResourceType type) {
            this.url = url;
            this.element = element;
            this.attributeName = attributeName;
            this.type = type;
        }
    }

    enum ResourceType { CSS_LINK, IMAGE, SCRIPT, INLINE_STYLE_ASSET, OTHER }

    /** Finds external resources (CSS, JS, Images) linked in the document */
    List<ResourceInfo> findExternalResources(Document doc);

    /** Finds resources referenced within inline style attributes */
    Map<Element, List<String>> findInlineStyleResources(Document doc); // Map Element -> List<asset urls>
}