package belittle;

import java.io.File;
import java.io.IOException;
import javax.imageio.stream.ImageInputStream;

public interface ResizableImageFactory {

  ResizableImage newResizeableImage(File file, BeLittleSizerSetting sizerSetting);

  ResizableImage newResizeableImage(ImageInputFile iif, BeLittleSizerSetting sizerSetting);

  ResizableImage newResizeableImage(ImageInputStream iis, BeLittleSizerSetting sizerSetting)
      throws IOException;
}
