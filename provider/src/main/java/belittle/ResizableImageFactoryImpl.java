package belittle;

import java.io.File;
import java.io.IOException;
import javax.imageio.stream.ImageInputStream;

/**
 * BUNDLE DEPENDENCIES
 *
 * <p>install -s mvn:com.google.guava/guava/23.0
 *
 * <p>install -s mvn:org.apache.commons/commons-exec/1.2
 *
 * <p>install -s mvn:com.github.jai-imageio/jai-imageio-jpeg2000/1.3.1_CODICE_3
 *
 * <p>install -s mvn:belittle/belittle-bundle/1.0-SNAPSHOT
 */
public class ResizableImageFactoryImpl implements ResizableImageFactory {

  ImageSizerCollection sizerCollection;

  public ResizableImageFactoryImpl(ImageSizerCollection collection) {
    this.sizerCollection = collection;
  }

  public ResizableImage newResizeableImage(File file) {
    return newResizeableImage(new ImageInputFileImpl(file));
  }

  public ResizableImage newResizeableImage(ImageInputFile iif) {
    return new ResizableImageImpl(sizerCollection).setInputFile(iif);
  }

  public ResizableImage newResizeableImage(ImageInputStream iis) throws IOException {
    return newResizeableImage(new ImageInputFileImpl(iis));
  }
}
