package main.java.com.educational.poc.LoginCloner.write;

import org.jsoup.nodes.Document;
import java.io.IOException;
import java.nio.file.Path;

public interface IDocumentWriter {
    boolean write(Document doc, Path filePath) throws IOException;
}