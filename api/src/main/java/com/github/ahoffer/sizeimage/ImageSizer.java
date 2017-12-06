package com.github.ahoffer.sizeimage;

import java.io.InputStream;
import java.util.Map;

/**
 * The ImageSizer interface is central to this library. Implementers of the interface can use any
 * strategy at all to resize an image. The expected functionality is:
 *
 * <ul>
 *   <li>Set and get an input stream that represents the image to be re-sized
 *   <li>Set and get configuration values
 *   <li>Set and get desired output generate
 *   <li>Perform the sizing operation and provided the re-sized image
 * </ul>
 *
 * For samples, see implementation included with this library.
 */
public interface ImageSizer {

  /**
   * Return the image sizer's configuration. It is recommended that implementors return an
   * unmodifiable view or a copy of the configuration, but it is not required.
   *
   * @return
   */
  Map<String, String> getConfiguration();

  /**
   * The configuration is expected to be a simple map of string keys to string values. One use is to
   * store the desired output generate of re-sized images. See implementers of this interface for
   * samples of how configuration can be used. It is expected implementers will copy the input and
   * not keep a reference to the input.
   *
   * @param configuration
   */
  // TODO: Blueprint expects the method to return void. Does a map w/ generics work in
  // todo: blueprint?  Gotta check it out.
  void setConfiguration(Map configuration);

  /**
   * Provide the image sizer with the image that should be re-sized.
   *
   * @param inputStream representing image
   * @return
   */
  ImageSizer setInput(InputStream inputStream);

  /**
   * Return the desired maximum width, in pixels.
   *
   * @return width in pixels
   */
  int getMaxWidth();

  /**
   * Return the desired maximum height, in pixels.
   *
   * @return height in pixels
   */
  int getMaxHeight();

  /**
   * Set the desired output generate. Always preserve aspect ratio. The sized image should fit into
   * a bounding box with dimensions maximum width x maximum height. The units are expected to be
   * pixels.
   *
   * @return this object
   */
  ImageSizer setOutputSize(int maxWidth, int maxHeight);

  /**
   * Resize the image associated with this image. Assume the image to be resized as passed as the
   * parameter of the setInput() method.
   *
   * @return resized image
   */
  BeLittlingResult generate();

  /**
   * Return true is the image sizer is ready and able generate images. Image sizers have
   * dependencies that might not be satisfied. For example, the image sizer could depend on a
   * external library, or on the availability of a web service. This method should return true if
   * the image sizers dependencies are all met.
   *
   * @return
   */
  default boolean isAvailable() {
    return true;
  }

  /**
   * Create a new instance of the concrete implementor of the image sizer. For thread safety, create
   * a new instance of sizer before using it.
   *
   * @return instance of a concrete image sizer
   */
  // TODO: Tried using scope=prototype in blueprint withgetServiceObjects(), but always same inst.
  ImageSizer getNew();

  ImageSizer addMessage(BeLittlingMessage message);

  ImageSizer setTimeoutSeconds(int seconds);

  int getTimeoutSeconds();
}
