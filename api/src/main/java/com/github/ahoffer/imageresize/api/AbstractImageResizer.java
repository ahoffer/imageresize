package com.github.ahoffer.imageresize.api;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.Validate;

public abstract class AbstractImageResizer implements ImageResizer {

    public static final String OUTPUT_SIZE_PIXELS = "outputSize";

    protected Map<String, String> configuration = new HashMap<>();

    public ImageResizer setConfiguration(Map<String, String> configuration) {
        // Add or replace configuration items
        this.configuration.putAll(configuration);
        return this;
    }

    public ImageResizer setOutputSize(int pixels) {
        configuration.put(OUTPUT_SIZE_PIXELS, Integer.toString(pixels));
        return this;
    }

    public void validateBeforeResize() {
        Validate.inclusiveBetween(1, Integer.MAX_VALUE, getOutputSize());
    }

    public int getOutputSize() {
        return Integer.valueOf(configuration.get(OUTPUT_SIZE_PIXELS));
    }

}