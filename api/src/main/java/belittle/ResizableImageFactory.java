package belittle;

import java.io.File;
import java.io.IOException;
import javax.imageio.stream.ImageInputStream;

public interface ResizableImageFactory {

  ResizableImage newResizeableImage(ImageInputStream iis) throws IOException;

  ResizableImage newResizeableImage(ImageInputFile iif);

  ResizableImage newResizeableImage(File file);
}
