package com.github.ahoffer.sizeimage;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public interface SizeImageService {

    Map<String, String> getConfiguration();

    SizeImageService setConfiguration(Map<String, String> configuration);

    SizeImageService setInput(InputStream inputStream);

    int getOutputSize();

    SizeImageService setOutputSize(int pixels);

    BufferedImage size() throws IOException;

    boolean recommendedFor(String imageFormat);

    default boolean isAvailable() {
        return true;
    }

    /**
     * Create a new instance of the concrete implementor of the image sizer. For thread safety,
     * create a new instance of sizer before using it. The reason is that the resziers can
     * be registered as OSGi services, and OSGI creates a single bean shared by all threads.
     *
     * @return instance of a concrete image resiezr
     */
    SizeImageService getNew();
}

