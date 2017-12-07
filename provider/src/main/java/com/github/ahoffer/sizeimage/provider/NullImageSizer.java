package com.github.ahoffer.sizeimage.provider;

import com.github.ahoffer.sizeimage.BeLittlingMessage;
import com.github.ahoffer.sizeimage.BeLittlingResult;
import com.github.ahoffer.sizeimage.ImageSizer;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;

/**
 * Null implementation of ImageSizer interface. Allows LittleWorker to be used without a real
 * ImageSizer
 *
 * @return
 */
class NullImageSizer implements ImageSizer {

  @Override
  public Map<String, String> getConfiguration() {
    return Collections.unmodifiableMap(Collections.emptyMap());
  }

  @Override
  public void setConfiguration(Map configuration) {}

  @Override
  public ImageSizer setInput(InputStream inputStream) {
    return this;
  }

  @Override
  public int getMaxWidth() {
    return 0;
  }

  @Override
  public int getMaxHeight() {
    return 0;
  }

  @Override
  public ImageSizer setOutputSize(int maxWidth, int maxHeight) {
    return this;
  }

  @Override
  public BeLittlingResult generate() {
    return null;
  }

  @Override
  public ImageSizer getNew() {
    return this;
  }

  @Override
  public ImageSizer addMessage(BeLittlingMessage message) {
    return this;
  }

  @Override
  public ImageSizer setTimeoutSeconds(int seconds) {
    return this;
  }

  @Override
  public int getTimeoutSeconds() {
    return 0;
  }
}
