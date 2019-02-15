package belittle.sizers;

import belittle.ImageInputFile;
import belittle.ImageSizer;
import java.awt.image.BufferedImage;
import java.io.File;

public class GdalSizer extends AbstractImageSizer {

  File executable;

  @Override
  public BufferedImage resize(ImageInputFile file) {
    return null;
  }

  @Override
  public ImageSizer getNew() {
    return new GdalSizer();
  }

  public File getExecutable() {
    return executable;
  }

  public void setExecutable(File executable) {
    this.executable = executable;
  }

  @Override
  public boolean isAvailable() {
    return getExecutable().canExecute();
  }
}
