package com.github.ahoffer.imageresize.provider;

import java.awt.image.BufferedImage;
import java.util.Map;

import com.github.ahoffer.imageresize.api.ImageResizer;

public class SamplingResizer extends AbstractImageResizer {

    public ImageResizer setConfiguration(Map<String, String> configuration) {
        return null;
    }

    public BufferedImage resize() {
        return null;
    }

    public boolean recommened(String imageFormat) {
        return false;
    }
}
