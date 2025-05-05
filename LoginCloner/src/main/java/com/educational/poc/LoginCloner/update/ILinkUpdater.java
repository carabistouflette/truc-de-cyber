package com.educational.poc.LoginCloner.update;

import org.jsoup.nodes.Element;
import java.nio.file.Path;

public interface ILinkUpdater {
    /**
     * Updates the attribute of an HTML element to point to a local path,
     * calculating the relative path from a source file.
     *
     * @param element       The HTML element to update.
     * @param attributeName The attribute to update (e.g., "href", "src").
     * @param localPath     The absolute path to the downloaded local resource.
     * @param sourcePath    The absolute path of the file containing the link (e.g., the HTML file).
     */
    void updateElementLink(Element element, String attributeName, Path localPath, Path sourcePath);

     /**
     * Updates a URL within an inline style attribute.
     *
     * @param element       The HTML element with the style attribute.
     * @param originalAssetUrl The original URL found in the style attribute.
     * @param localPath     The absolute path to the downloaded local resource.
     * @param sourcePath    The absolute path of the file containing the link (e.g., the HTML file).
     */
    void updateInlineStyleLink(Element element, String originalAssetUrl, Path localPath, Path sourcePath);
}