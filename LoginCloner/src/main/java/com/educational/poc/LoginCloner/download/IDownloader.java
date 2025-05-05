package com.educational.poc.LoginCloner.download;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

public interface IDownloader {
    /**
     * Downloads a resource from a URL.
     * @param resourceUrlStr URL to download.
     * @return InputStream of the downloaded content. Null if download fails.
     * @throws IOException if an IO error occurs during connection or download.
     */
    InputStream download(String resourceUrlStr) throws IOException;
}