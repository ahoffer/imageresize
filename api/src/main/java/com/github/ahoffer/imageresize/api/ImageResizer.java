package com.github.ahoffer.imageresize.api;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.Map;

public interface ImageResizer {

    ImageResizer setConfiguration(Map<String, String> configuration);

    ImageResizer setInput(InputStream inputStream);

    ImageResizer setImageIndex(int imageIndex);

    ImageResizer setSize(int pixels);

    BufferedImage resize();

    boolean recommened(String imageFormat);

}

