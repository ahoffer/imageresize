package belittle;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;
import javax.imageio.stream.ImageInputStream;

public interface BeLittle {

  /** Convenience method. Attempt to generate an image from the first available ImageSizer. */
  BufferedImage resize(ImageInputStream iis) throws RuntimeException;

  BufferedImage resize(File file) throws RuntimeException;

  List<BeLittleResult> getLastResults();
}
