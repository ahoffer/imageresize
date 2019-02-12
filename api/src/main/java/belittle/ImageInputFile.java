package belittle;

import java.io.File;
import java.io.InputStream;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

public interface ImageInputFile extends AutoCloseable {

  boolean doWithInputStream(IoConsumer<InputStream> consumer);

  boolean doWithImageInputStream(IoConsumer<ImageInputStream> consumer);

  boolean doWithImageReader(IoConsumer<ImageReader> consumer);

  String getMimeType();

  File getFile();

  String getResolvedPath();

  boolean copyTo(File destination);
}
