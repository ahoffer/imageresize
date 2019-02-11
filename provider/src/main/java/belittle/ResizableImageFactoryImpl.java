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
  BeLittleSizerSetting sizerSetting;

  public ResizableImageFactoryImpl(BeLittleSizerSetting setting, ImageSizerCollection collection) {
    this.sizerCollection = collection;
    this.sizerSetting = setting;
  }

  public ResizableImage newResizeableImage(File file, BeLittleSizerSetting sizerSetting) {
    return new ResizableImageImpl(new ImageInputFileImpl(file), sizerSetting, sizerCollection);
  }

  public ResizableImage newResizeableImage(ImageInputFile iif, BeLittleSizerSetting sizerSetting) {
    return new ResizableImageImpl(iif, sizerSetting, sizerCollection);
  }

  public ResizableImage newResizeableImage(ImageInputStream iis, BeLittleSizerSetting sizerSetting)
      throws IOException {
    return new ResizableImageImpl(new ImageInputFileImpl(iis), sizerSetting, sizerCollection);
  }
}
