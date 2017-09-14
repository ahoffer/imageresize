package com.github.ahoffer.sizeimage.provider;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.Validate;

import com.github.ahoffer.sizeimage.SizeImageService;

public abstract class AbstractSizeImageService implements SizeImageService {

    public static final String OUTPUT_SIZE_PIXELS = "outputSize";

    protected Map<String, String> configuration = new HashMap<>();

    public Map<String, String> getConfiguration() {
        return Collections.unmodifiableMap(configuration);
    }

    public SizeImageService setConfiguration(Map<String, String> configuration) {
        // Add or replace configuration items
        this.configuration.putAll(configuration);
        return this;
    }

    public SizeImageService setOutputSize(int pixels) {
        configuration.put(OUTPUT_SIZE_PIXELS, Integer.toString(pixels));
        return this;
    }

    public void validateBeforeSize() {
        Validate.inclusiveBetween(1, Integer.MAX_VALUE, getOutputSize());
    }

    public int getOutputSize() {
        try {
            return Integer.valueOf(configuration.get(OUTPUT_SIZE_PIXELS));
        } catch (NumberFormatException e) {
            throw new RuntimeException("Cannot read output dimensions for image size", e);
        }

    }

    public SizeImageService getNew() {
        try {
            SizeImageService newInstance = getClass().newInstance();
            newInstance.setConfiguration(configuration);
            return newInstance;
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
