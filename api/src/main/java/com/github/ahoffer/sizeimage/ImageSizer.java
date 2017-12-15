package com.github.ahoffer.sizeimage;

import java.io.InputStream;
import java.util.Map;

/**
 * The ImageSizer interface is central class of this library. Implementers of the interface can use
 * any method to resize an image. The responsibilities of an ImageSizer are
 *
 * <ul>
 *   <li>Set and get an input stream that represents the image to be re-sized
 *   <li>Perform the sizing operation and provide the re-sized image, along with any messages the
 *       generated during the process
 *   <li>Allow other objects to add messages to the sizer during the process
 *   <li>
 *   <li>Set and get configuration values. At the interface level, the defined configurations are:
 *       <ul>
 *         <li>Desired width and height (in pixels) of resized image
 *         <li>Desired height of resized image
 *         <li>Maximum allowed wall clock time (in seconds) to resize an image. Operations exceeding
 *             the allowed time should be cancelled
 *       </ul>
 *   <li>
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
   * Return the desired maximum width, in pixels. Resized image may have smaller than the maximum
   * width to preserve aspect ratio.
   *
   * @return maximum width of resized image, in pixels
   */
  int getMaxWidth();

  /**
   * Return the desired maximum height, in pixels. Resized image may have smaller than the maximum
   * height to preserve aspect ratio.
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
   * Create a new instance of the concrete implementor of the image sizer. ImageSizers are intended
   * to prototype objects. To use a sizer, clone an existing sizer. The cloning function should
   * preserve the configuration of the sizer, but not the transient/mutable objects, such as the
   * input image and the messages associated with the sizer. Cloning ImageSizers promotes thread
   * safety; a particular ImageSizer objects should only be accessed by a single thread.
   *
   * @return instance of a concrete image sizer
   */
  // TODO: Tried using scope=prototype in blueprint. Then fetch it using getServiceObjects(), but
  // always same instance. Work-around was to clone the ImageSizer before returning it to a
  // consumer.
  ImageSizer getNew();

  /**
   * Allows cooperating objects the opportunity to add messages to the sizer about errors, potential
   * problems, or information.
   *
   * @param message
   * @return the image sizer
   */
  ImageSizer addMessage(BeLittlingMessage message);

  /**
   * Prevent the ImageSizer run indefinitely. Limit its execution time to a certain number of
   * seconds.
   *
   * @param seconds
   * @return the image sizer
   */
  ImageSizer setTimeoutSeconds(int seconds);

  /**
   * The number of seconds the ImageSizer should run before giving up and returning control to the
   * caller.
   *
   * @return
   */
  int getTimeoutSeconds();
}
