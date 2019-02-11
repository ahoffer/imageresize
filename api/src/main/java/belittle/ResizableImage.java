package belittle;

import java.awt.image.BufferedImage;
import java.util.List;

/** */
public interface ResizableImage {

  BufferedImage resize(int width, int height, boolean preserveAspectRatio);

  List<BeLittleResult> getResults();
}
