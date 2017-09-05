package com.github.ahoffer.imageresize.api;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public interface ImageResizer {

    ImageResizer setConfiguration(Map<String, String> configuration);

    ImageResizer setInput(InputStream inputStream);

    ImageResizer setOutputSize(int pixels);

    int getOutputSize();

    BufferedImage resize() throws IOException;

    boolean recommendedFor(String imageFormat);


}

