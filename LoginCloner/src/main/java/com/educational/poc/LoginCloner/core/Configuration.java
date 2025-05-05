package main.java.com.educational.poc.LoginCloner.core;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Configuration {
    // Base directory relative to execution
    private static final String BASE_OUTPUT_DIR = ".";
    // Parent directory for all output
    public static final Path CLONED_PAGE_DIR = Paths.get(BASE_OUTPUT_DIR, "cloned_page");
    // HTML file inside cloned_page
    public static final Path OUTPUT_FILE_PATH = CLONED_PAGE_DIR.resolve("cloned_login_page.html");
    // Assets inside cloned_page
    public static final Path ASSETS_DIR = CLONED_PAGE_DIR.resolve("cloned_assets");
    // css inside cloned_page/cloned_assets
    public static final Path CSS_DIR = ASSETS_DIR.resolve("css");
    // images inside cloned_page/cloned_assets
    public static final Path IMG_DIR = ASSETS_DIR.resolve("images");
    // other inside cloned_page/cloned_assets
    public static final Path OTHER_DIR = ASSETS_DIR.resolve("other");
    // scripts inside cloned_page/cloned_assets (if processing scripts)
    public static final Path SCRIPT_DIR = ASSETS_DIR.resolve("scripts");

    // Server configuration
    public static final int SERVER_PORT = 8080;
    public static final String LOGIN_HANDLER_PATH = "/handle_login";
    public static final String SERVER_ACTION_URL = "http://localhost:" + SERVER_PORT + LOGIN_HANDLER_PATH;

    // Download configuration
    public static final int CONNECT_TIMEOUT_MS = 10000; // 10 seconds
    public static final int READ_TIMEOUT_MS = 15000; // 15 seconds
    public static final String USER_AGENT = "LoginCloner/1.0 (Educational POC)";
}