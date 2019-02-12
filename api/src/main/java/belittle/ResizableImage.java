package belittle;

import java.awt.image.BufferedImage;
import java.util.List;

/** */
public interface ResizableImage extends AutoCloseable {

  BufferedImage resize();

  List<BeLittleResult> getResults();

  ImageInputFile getInputFile();

  ResizableImage setInputFile(ImageInputFile file);

  int getTimeoutSeconds();

  ResizableImage setTimeoutSeconds(int seconds);

  int getWidth();

  ResizableImage setWidth(int width);

  int getHeight();

  ResizableImage setHeight(int height);
}
