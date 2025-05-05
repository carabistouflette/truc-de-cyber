package com.educational.poc.LoginCloner.detect;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public interface IFormDetector {
    Element findLoginForm(Document doc);
    void modifyLoginFormAction(Element loginForm, String newActionUrl, String method);
}