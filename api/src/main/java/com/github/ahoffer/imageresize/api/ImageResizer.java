package com.github.ahoffer.imageresize.api;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public interface ImageResizer {

    Map<String, String> getConfiguration();

    ImageResizer setConfiguration(Map<String, String> configuration);

    ImageResizer setInput(InputStream inputStream);

    int getOutputSize();

    ImageResizer setOutputSize(int pixels);

    BufferedImage resize() throws IOException;

    boolean recommendedFor(String imageFormat);

    default boolean isAvailable() {
        return true;
    }

    /**
     * Create a new instance of the concrete implementor of the image resizer. For thread safety,
     * create a new instance of resizer before using it. The reason is that the resziers can
     * be registered as OSGi services, and OSGI creates a single bean shared by all threads.
     *
     * @return instance of a concrete image resiezr
     */
    ImageResizer getNew();
}

