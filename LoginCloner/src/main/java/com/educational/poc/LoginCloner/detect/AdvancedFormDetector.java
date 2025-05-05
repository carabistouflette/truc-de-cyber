package com.educational.poc.LoginCloner.detect;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.jsoup.select.Selector.SelectorParseException; // Import specific exception

/**
 * Detects login forms using a scoring mechanism based on various heuristics.
 */
public class AdvancedFormDetector implements IFormDetector {

    // Keywords for scoring (consider making these configurable or constants)
    private static final String[] USER_KEYWORDS = {"user", "email", "login", "account", "identifier", "username", "userid", "e-mail", "mail"};
    private static final String[] PASS_KEYWORDS = {"pass", "secret", "credential", "password", "passwd"};
    private static final String[] ACTION_KEYWORDS = {"login", "signin", "auth", "authenticate", "session", "account/login", "wp-login", "logon"};
    private static final String[] SUBMIT_KEYWORDS = {"login", "sign in", "log in", "submit", "go", "enter", "continue"};
    private static final String[] STRUCTURAL_KEYWORDS = {"login", "signin", "auth", "account", "user", "credential"}; // For parent/container checks
    private static final String[] HONEYPOT_STYLE_INDICATORS = {"display:none", "visibility:hidden", "opacity:0"}; // CSS for hidden fields
    private static final String[] HONEYPOT_NAME_KEYWORDS = {"confirm_email", "website", "url", "phone", "comment", "address", "homepage"}; // Common honeypot names

    @Override
    public Element findLoginForm(Document doc) {
        if (doc == null) {
            System.err.println("Cannot find login form: Document is null.");
            return null;
        }

        Elements forms = doc.select("form");
        Element bestForm = null;
        int bestScore = -1;

        System.out.println("Analyzing " + forms.size() + " forms for login characteristics...");

        for (Element form : forms) {
            int currentScore = 0;
            boolean hasPasswordInputType = false;
            boolean hasUserInputType = false; // Includes text, email, tel
            boolean hasPasswordNameOrId = false;
            boolean hasUserNameOrId = false;
            boolean hasPasswordLabel = false;
            boolean hasUserLabel = false;
            boolean likelyHoneypotDetected = false;
            int emailFieldCount = 0; // To help identify honeypots like 'confirm_email'

            // --- 1. Analyze Inputs, Labels, and Honeypots ---
            Elements inputs = form.select("input, textarea"); // Include textarea just in case
            for (Element input : inputs) {
                String type = input.attr("type").toLowerCase();
                String name = input.attr("name").toLowerCase();
                String id = input.attr("id").toLowerCase();
                String style = input.attr("style").replaceAll("\\s", "").toLowerCase(); // Remove whitespace for style check

                // A. Check Input Type and Name/ID
                if (type.equals("password")) {
                    hasPasswordInputType = true;
                    for (String keyword : PASS_KEYWORDS) {
                        if (name.contains(keyword) || id.contains(keyword)) {
                            hasPasswordNameOrId = true;
                            break;
                        }
                    }
                } else if (type.equals("text") || type.equals("email") || type.equals("tel")) {
                    hasUserInputType = true;
                    if (type.equals("email")) emailFieldCount++;
                    for (String keyword : USER_KEYWORDS) {
                        if (name.contains(keyword) || id.contains(keyword)) {
                            hasUserNameOrId = true;
                            break;
                        }
                    }
                }

                // B. Check Associated Labels
                if (!id.isEmpty()) {
                    // Try finding label using "for" attribute
                    // Escape potential special characters in ID for CSS selector
                    String escapedId = id.replaceAll("([\\!\"#$%&'\\(\\)\\*\\+,\\.\\/:;<=>\\?@\\[\\\\\\]\\^`\\{\\|\\}~])", "\\\\$1");
                    Element label = null;
                    try {
                         label = form.selectFirst("label[for=" + escapedId + "]");
                    } catch (SelectorParseException e) { // Catch specific exception
                         System.err.println("    Warning: Could not parse selector for label with id '" + id + "': " + e.getMessage());
                    }

                    // Fallback: Check if label directly contains the input (less reliable)
                    if (label == null) {
                        Element parentLabel = input.parent();
                        if (parentLabel != null && parentLabel.tagName().equalsIgnoreCase("label")) {
                            label = parentLabel;
                        }
                    }

                    if (label != null) {
                        String labelText = label.text().toLowerCase();
                        if (!hasUserLabel) { // Only score once per form for label type
                            for (String keyword : USER_KEYWORDS) {
                                if (labelText.contains(keyword)) {
                                    hasUserLabel = true;
                                    break;
                                }
                            }
                        }
                        if (!hasPasswordLabel) { // Only score once per form for label type
                             for (String keyword : PASS_KEYWORDS) {
                                if (labelText.contains(keyword)) {
                                    hasPasswordLabel = true;
                                    break;
                                }
                            }
                        }
                    }
                }

                // C. Check for Honeypots
                // Check 1: Hidden by inline style
                for (String indicator : HONEYPOT_STYLE_INDICATORS) {
                    if (style.contains(indicator)) {
                        System.out.println("    -> Potential honeypot detected (hidden style): input name='" + name + "', id='" + id + "' in form (" + form.attr("id") + "/" + form.attr("name") + ")");
                        likelyHoneypotDetected = true;
                        break; // Found one indicator, no need to check others for this input
                    }
                }
                // Check 2: Common honeypot names (especially if not expected, e.g., confirm_email when only one email field exists)
                if (!likelyHoneypotDetected) {
                    for (String keyword : HONEYPOT_NAME_KEYWORDS) {
                        if (name.contains(keyword) || id.contains(keyword)) {
                            // Be stricter with 'confirm_email' if only one email field is present
                            if (keyword.equals("confirm_email") && emailFieldCount <= 1) {
                                System.out.println("    -> Potential honeypot detected (name '" + keyword + "' with single email field): input name='" + name + "', id='" + id + "' in form (" + form.attr("id") + "/" + form.attr("name") + ")");
                                likelyHoneypotDetected = true;
                                break;
                            } else if (!keyword.equals("confirm_email")) { // Apply penalty for other names
                                System.out.println("    -> Potential honeypot detected (name '" + keyword + "'): input name='" + name + "', id='" + id + "' in form (" + form.attr("id") + "/" + form.attr("name") + ")");
                                likelyHoneypotDetected = true;
                                break;
                            }
                        }
                    }
                }
                 // If honeypot found in this form, stop checking its inputs for honeypots, but continue checking for user/pass fields
                 // if (likelyHoneypotDetected) break; // Decided against breaking early to still gather positive signals

            } // End input loop

            // --- 2. Analyze Form Attributes (Action, Method) ---
            String action = form.attr("action").toLowerCase();
            String method = form.attr("method").toLowerCase();

            // Check action attribute
            if (!action.isEmpty()) {
                for (String keyword : ACTION_KEYWORDS) {
                    if (action.contains(keyword)) {
                        currentScore += 3; // Increased score for relevant action
                        break;
                    }
                }
            }

            // Check method attribute (Prefer POST)
            if (method.equals("post")) {
                currentScore += 4; // Significant points for using POST
            }

            // --- 3. Analyze Submit Buttons ---
            Elements submitButtons = form.select("input[type=submit], button[type=submit], button:not([type]), input[type=button]"); // Broader selection
            if (!submitButtons.isEmpty()) {
                 boolean submitKeywordFound = false;
                 for(Element button : submitButtons) {
                     String text = button.text().toLowerCase();
                     String value = button.attr("value").toLowerCase();
                     String name = button.attr("name").toLowerCase();
                     String id = button.attr("id").toLowerCase();
                     for (String keyword : SUBMIT_KEYWORDS) {
                         // Check text, value, name, id
                         if (text.contains(keyword) || value.contains(keyword) || name.contains(keyword) || id.contains(keyword)) {
                             currentScore += 2; // Increased score for submit button
                             submitKeywordFound = true;
                             break;
                         }
                     }
                     if(submitKeywordFound) break; // Only award points once per form for submit button
                 }
            }

            // --- 4. Analyze Structural Clues (Parent Element) ---
            Element parent = form.parent();
            if (parent != null) {
                String parentId = parent.id().toLowerCase();
                // String parentClass = parent.className().toLowerCase(); // Less direct than checking classNames()
                boolean structuralKeywordFound = false;
                for (String keyword : STRUCTURAL_KEYWORDS) {
                    // Check full class list for keyword match
                    boolean classMatch = false;
                    for (String cls : parent.classNames()) {
                        if (cls.toLowerCase().contains(keyword)) {
                            classMatch = true;
                            break;
                        }
                    }
                    if (parentId.contains(keyword) || classMatch) {
                        currentScore += 3; // Points for relevant container ID/class
                        structuralKeywordFound = true;
                        break;
                    }
                }
                // Optional: Check siblings too, but parent is often enough
            }

            // --- 5. Calculate Final Score ---
            // Base points from checks above
            int finalScore = currentScore;

            // Add points for labels found
            if (hasUserLabel) finalScore += 2;
            if (hasPasswordLabel) finalScore += 2;

            // Major boost if both essential input *types* are present
            if (hasPasswordInputType && hasUserInputType) {
                finalScore += 8;
            }
            // Additional boost if names/IDs also match
            if (hasPasswordNameOrId && hasUserNameOrId) {
                 finalScore += 5;
            } else if (hasPasswordNameOrId || hasUserNameOrId) { // Boost if at least one name matches
                 finalScore += 2;
            }


            // Apply Honeypot Penalty
            if (likelyHoneypotDetected) {
                finalScore -= 20; // Heavy penalty for suspected honeypots
                System.out.println("    -> Applying honeypot penalty.");
            }

            // --- Score Evaluation ---
            System.out.println("  Form (" + form.attr("id") + "/" + form.attr("name") + ") Score: " + finalScore +
                               " (PwdType:" + hasPasswordInputType + ", UserType:" + hasUserInputType +
                               ", PwdName:" + hasPasswordNameOrId + ", UserName:" + hasUserNameOrId +
                               ", PwdLbl:" + hasPasswordLabel + ", UserLbl:" + hasUserLabel +
                               ", POST:" + method.equals("post") + ", Honeypot:" + likelyHoneypotDetected + ")");

            // Update best form if current score is higher
            if (finalScore > bestScore) {
                 // Basic check to avoid tiny, likely irrelevant forms (can be refined)
                 // Allow forms with few inputs if score is high (e.g. just user/pass/submit)
                 if (form.select("input").size() < 2 && finalScore < 10) { // Stricter condition for skipping
                     System.out.println("    -> Skipping potentially trivial form (very few inputs/low score).");
                 } else {
                     bestScore = finalScore;
                     bestForm = form;
                     System.out.println("    -> New best candidate found.");
                 }
            }
        } // End form loop

        if (bestForm != null) {
             System.out.println("Selected form with score " + bestScore + " as the most likely login form.");
        } else {
             System.out.println("No suitable login form found based on scoring criteria.");
        }

        return bestForm;
    }

    @Override
    public void modifyLoginFormAction(Element loginForm, String newActionUrl, String method) {
         if (loginForm == null) {
              System.err.println("Cannot modify action: provided login form element is null.");
              return;
         }
         if (newActionUrl == null || newActionUrl.trim().isEmpty()) {
              System.err.println("Cannot modify action: newActionUrl is null or empty.");
              return;
         }
         if (method == null || (!method.equalsIgnoreCase("POST") && !method.equalsIgnoreCase("GET"))) {
              System.err.println("Cannot modify method: method must be POST or GET.");
              return;
         }

        loginForm.attr("action", newActionUrl); // Set or update the action attribute
        loginForm.attr("method", method.toUpperCase());      // Set or update the method attribute
        System.out.println("Login form action modified to '" + newActionUrl + "' and method to '" + method.toUpperCase() + "'.");
    }
}