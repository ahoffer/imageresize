package com.github.ahoffer.sizeimage.provider;

import com.github.ahoffer.sizeimage.ImageSizer;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.lang3.Validate;

public abstract class AbstractImageSizer implements ImageSizer {

  public static final String MAX_WIDTH = "maxWidth";
  public static final String MAX_HEIGHT = "maxHeight";

  protected Map<String, String> configuration = new HashMap<>();
  protected InputStream inputStream;

  public ImageSizer setOutputSize(int maxWidth, int maxHeight) {
    validateExtents(maxWidth, maxHeight);
    configuration.put(MAX_WIDTH, Integer.toString(maxWidth));
    configuration.put(MAX_HEIGHT, Integer.toString(maxHeight));
    return this;
  }

  private void validateExtents(int maxWidth, int maxHeight) {
    Validate.inclusiveBetween(1, Integer.MAX_VALUE, maxWidth);
    Validate.inclusiveBetween(1, Integer.MAX_VALUE, maxHeight);
  }

  public Map<String, String> getConfiguration() {
    return Collections.unmodifiableMap(configuration);
  }

  public void setConfiguration(Map<String, String> configuration) {
    Map copy = Optional.ofNullable(configuration).map(x -> new HashMap(x)).orElseGet(HashMap::new);
    this.configuration = copy;
  }

  public void validateBeforeResizing() {
    validateExtents(getMaxWidth(), getMaxHeight());
    Validate.notNull(inputStream);
  }

  public int getMaxWidth() {
    return getInteger(configuration.get(MAX_WIDTH));
  }

  private int getInteger(String intString) {
    try {
      return Integer.valueOf(intString);
    } catch (NumberFormatException e) {
      throw new RuntimeException("Cannot convert string to integer", e);
    }
  }

  public int getMaxHeight() {
    return getInteger(configuration.get(MAX_HEIGHT));
  }

  public ImageSizer getNew() {
    try {
      // This works in some situations where clone() does not
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
