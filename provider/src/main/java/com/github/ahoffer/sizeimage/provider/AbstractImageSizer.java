package com.github.ahoffer.sizeimage.provider;

import com.github.ahoffer.sizeimage.ImageSizer;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.Validate;

public abstract class AbstractImageSizer implements ImageSizer {

  public static final String OUTPUT_SIZE_PIXELS = "outputSize";

  public static final String JPEG_2000_FORMAT_NAME = "jpeg 2000";

  protected Map<String, String> configuration = new HashMap<>();

  protected InputStream inputStream;

  public Map<String, String> getConfiguration() {
    return Collections.unmodifiableMap(configuration);
  }

  public ImageSizer setConfiguration(Map<String, String> configuration) {
    // Add or replace configuration items
    this.configuration.putAll(configuration);
    return this;
  }

  public ImageSizer setOutputSize(int pixels) {
    configuration.put(OUTPUT_SIZE_PIXELS, Integer.toString(pixels));
    return this;
  }

  public void validateBeforeResizing() {
    Validate.notNull(inputStream);
    Validate.inclusiveBetween(1, Integer.MAX_VALUE, getOutputSize());
  }

  public int getOutputSize() {
    try {
      return Integer.valueOf(configuration.get(OUTPUT_SIZE_PIXELS));
    } catch (NumberFormatException e) {
      throw new RuntimeException("Cannot read output dimensions for image size", e);
    }
  }

  public ImageSizer getNew() {
    try {
      ImageSizer newInstance = getClass().newInstance();
      newInstance.setConfiguration(configuration);
      return newInstance;
    } catch (InstantiationException | IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  public ImageSizer setInput(InputStream inputStream) {
    this.inputStream = inputStream;
    return this;
  }
}
