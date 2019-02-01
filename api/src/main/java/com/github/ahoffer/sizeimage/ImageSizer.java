package com.github.ahoffer.sizeimage;

import java.io.InputStream;

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
   * Primary method
   *
   * @param inputStream
   * @return
   */
  BeLittlingResult resize(InputStream inputStream);

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
  ImageSizer getNew(BeLittleSizerSetting sizerSetting, BeLittlingResult injectedResult);

  ImageSizer getNew();
}
