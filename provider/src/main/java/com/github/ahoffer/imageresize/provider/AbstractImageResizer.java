package com.github.ahoffer.imageresize.provider;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.Validate;

import com.github.ahoffer.imageresize.api.ImageResizer;

public abstract class AbstractImageResizer implements ImageResizer {

    public static final String OUTPUT_SIZE_PIXELS = "outputSize";

    protected Map<String, String> configuration = new HashMap<>();

    public Map<String, String> getConfiguration() {
        return Collections.unmodifiableMap(configuration);
    }

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
        try {
            return Integer.valueOf(configuration.get(OUTPUT_SIZE_PIXELS));
        } catch (NumberFormatException e) {
            throw new RuntimeException("Cannot read output dimensions for image resize", e);
        }

    }

    public ImageResizer getNew() {
        try {
            ImageResizer newInstance = getClass().newInstance();
            newInstance.setConfiguration(configuration);
            return newInstance;
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
