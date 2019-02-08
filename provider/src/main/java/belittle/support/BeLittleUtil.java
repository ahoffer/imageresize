package belittle.support;

import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;

// TODO: Create a class called FileBasedImage and move these methods there.

public class BeLittleUtil {

  public void doWithInputStream(File file, IoConsumer<InputStream> consumer) throws IOException {
    try (InputStream istream = Files.asByteSource(file).openStream()) {
      consumer.accept(istream);
    }
  }

  public void doWithImageInputStream(File file, IoConsumer<ImageInputStream> consumer)
      throws IOException {
    doWithInputStream(
        file,
        (istream) -> {
          ImageInputStream iis = null;
          try {
            iis = ImageIO.createImageInputStream(istream);
            consumer.accept(iis);

          } finally {
            closeImageInputStream(iis);
          }
        });
  }

  protected void closeImageInputStream(ImageInputStream iis) throws IOException {
    if (iis != null) {
      iis.close();
    }
  }
}
