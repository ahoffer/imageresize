package com.github.ahoffer.sizeimage.provider;

import com.github.ahoffer.sizeimage.ImageSizer;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import org.apache.commons.lang3.Validate;

public abstract class AbstractImageSizer implements ImageSizer {

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    AbstractImageSizer that = (AbstractImageSizer) o;

    return configuration.equals(that.configuration);
  }

  @Override
  public int hashCode() {
    return configuration.hashCode();
  }

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
    final Map copy =
        Optional.ofNullable(configuration)
            .map((Function<Map<String, String>, HashMap>) HashMap::new)
            .orElseGet(HashMap::new);
    this.configuration = copy;
  }

  public void validateBeforeResizing() {
    validateExtents(getMaxWidth(), getMaxHeight());
    if (Objects.isNull(inputStream)) {
      throw new IllegalArgumentException("Input stream cannot be null");
    }
  }

  public int getMaxWidth() {
    return getInteger(configuration.get(MAX_WIDTH));
  }

  private int getInteger(String intString) {
    return Integer.valueOf(intString);
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
      throw new CopyObjectException(e);
    }
  }

  public ImageSizer setInput(InputStream inputStream) {
    this.inputStream = inputStream;
    return this;
  }

  public class CopyObjectException extends RuntimeException {

    CopyObjectException(Throwable cause) {
      super(cause);
    }
  }
}
