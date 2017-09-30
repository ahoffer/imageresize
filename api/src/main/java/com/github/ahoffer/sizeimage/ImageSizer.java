package com.github.ahoffer.sizeimage;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public interface ImageSizer {

  @SuppressWarnings("unused")
  Map<String, String> getConfiguration();

  ImageSizer setConfiguration(Map<String, String> configuration);

  ImageSizer setInput(InputStream inputStream);

  int getMaxWidth();

  int getMaxHeight();

  /**
   * Always preserve aspect ratio. The sized image should fit into a bounding box with dimensions
   * size x size
   *
   * @return this object
   */
  @SuppressWarnings("unused")
  ImageSizer setOutputSize(int maxWidth, int maxHeight);

  BufferedImage size() throws IOException;

  default boolean isAvailable() {
    return true;
  }

  /**
   * Create a new instance of the concrete implementor of the image sizer. For thread safety, create
   * a new instance of sizer before using test. The reason is that the sizers can be registered as
   * OSGi services, and OSGI creates a single bean shared by all threads.
   *
   * @return instance of a concrete image sizer
   */
  // TODO: I tried using scope=prototype in the blueprint, then using getServiceObjects(), but I
  // still got the same instance.
  ImageSizer getNew();
}
