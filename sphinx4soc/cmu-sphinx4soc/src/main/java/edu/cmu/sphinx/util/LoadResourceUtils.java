package edu.cmu.sphinx.util;

import java.net.MalformedURLException;
import java.net.URL;

public class LoadResourceUtils {

    private LoadResourceUtils() {
    }

    public static URL load(String path) throws MalformedURLException {

        URL url = LoadResourceUtils.class.getClass().getResource(path);

        try {
            if (url.getProtocol().equalsIgnoreCase("jar")) {
                return new URL(url.getFile());
            } else if (url.getProtocol().equalsIgnoreCase("resource")) {
                return new URL(url.getFile());
            } else if (url.getProtocol().equalsIgnoreCase("file")) {
                return url;
            } else {
                throw new Exception("Something is wrong with configuration  " + path);
            }
        } catch (Exception e) {
            throw new MalformedURLException("Something is wrong with configuration  " + path);
        }
    }
}
