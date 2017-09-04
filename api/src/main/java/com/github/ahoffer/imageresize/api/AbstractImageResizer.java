package com.github.ahoffer.imageresize.api;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.Validate;

public abstract class AbstractImageResizer implements ImageResizer {

    public static final String OUTPUT_SIZE_PIXELS = "outputSize";

    Map<String, String> configuration = new HashMap<>();

    public ImageResizer setConfiguration(Map<String, String> configuration)

    {
        // Add or replace configuration
        configuration.putAll(configuration);
        return this;
    }

    public ImageResizer setInput(InputStream inputStream) {
        return null;
    }

    public ImageResizer setOutputSize(int pixels) {
        return null;
    }

    public void validateBeforeResize() {
        Validate.inclusiveBetween(1, Integer.MAX_VALUE, getOutputSize());
    }

    public int getOutputSize() {
        return Integer.valueOf(configuration.get(OUTPUT_SIZE_PIXELS));
    }

}
