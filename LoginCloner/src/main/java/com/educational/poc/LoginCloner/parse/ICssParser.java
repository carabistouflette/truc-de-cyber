package com.educational.poc.LoginCloner.parse;

import java.util.List;

public interface ICssParser {
    /** Finds url() references within CSS content */
    List<String> findResourceUrls(String cssContent);

    /** Replaces an old URL with a new URL within CSS content */
    String replaceUrl(String cssContent, String oldUrl, String newUrl);
}