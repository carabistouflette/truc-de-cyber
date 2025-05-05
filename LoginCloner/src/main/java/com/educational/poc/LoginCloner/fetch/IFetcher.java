package com.educational.poc.LoginCloner.fetch;

import org.jsoup.nodes.Document;
import java.io.IOException;

public interface IFetcher {
    Document fetch(String urlString) throws IOException;
}