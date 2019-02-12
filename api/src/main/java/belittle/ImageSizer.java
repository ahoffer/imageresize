package belittle;

import java.awt.image.BufferedImage;

public interface ImageSizer {

  BufferedImage resize(int width, int height, ImageInputFile file);

  /**
   * Create a new instance of the concrete implementor of the image sizer. ImageSizers are intended
   * to prototype objects. To use a sizer, clone an existing sizer. The cloning function should
   * preserve the configuration of the sizer, but not the transient/mutable objects, such as the
   * input image and the messages associated with the sizer. Cloning ImageSizers promotes thread
   * safety; a particular ImageSizer objects should only be accessed by a single thread.
   *
   * @return instance of a concrete image sizer
   */
  ImageSizer getNew();

  BeLittleResult getResult();

  void addMessage(BeLittleMessage message);
}
