package edu.cmu.sphinx.util;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.log4j.Logger;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

import edu.cmu.sphinx.linguist.acoustic.tiedstate.Sphinx3Loader;

public class LoadResourceUtils {

    static final Logger logger = Logger.getLogger( Sphinx3Loader.class );
    
    private LoadResourceUtils() {
    }

    public static URL load(String path) throws MalformedURLException {

        URL url = LoadResourceUtils.class.getClass().getResource(path);
        logger.info("############################ Path resource " + path + " url " + url.toString());

        try {
            if (url.getProtocol().equalsIgnoreCase("jar")) {
                return new URL(url.getFile());
            } else if (url.getProtocol().equalsIgnoreCase("resource")) {
                return new URL(url.getFile());
            } else if (url.getProtocol().equalsIgnoreCase("file")) {
                return url;
            } else if (url.getProtocol().equalsIgnoreCase("bundle")) {
                BundleContext context = FrameworkUtil.getBundle(LoadResourceUtils.class).getBundleContext();
                URL entry = context.getBundle().getEntry(path);
                logger.info("############################ Bundle resource " + entry.toString());
               return entry;
            } else {
                return url;
            }
        } catch (Exception e) {
            throw new MalformedURLException("Something is wrong with configuration  " + path);
        }
    }
}
