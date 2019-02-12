package belittle.sizers;

import belittle.ImageInputFile;
import belittle.ImageSizer;
import belittle.support.ComputeSubSamplingPeriod;
import java.awt.image.BufferedImage;
import javax.imageio.ImageReader;
import net.coobird.thumbnailator.Thumbnails;

public class SamplingSizer extends AbstractImageSizer {

  public static final String SAMPLING_PERIOD_KEY = "samplingPeriod";

  private Integer samplingPeriod;
  private ImageReader reader;

  void cleanup() {
    if (reader != null) {
      reader.dispose();
    }
  }

  @Override
  public boolean isAvailable() {
    return true;
  }

  @Override
  public BufferedImage resize(ImageInputFile file) {
    file.doWithImageReader(
        (reader) -> {
          int inputHeight = reader.getHeight(0);
          int inputWidth = reader.getWidth(0);
          if (inputHeight <= 0 || inputWidth <= 0) {
            addError(
                "Width and/or height of input image cannot be determined and no "
                    + "default sampling period is value is configured. Sampling sizer needs "
                    + "these values to compute subsampling period.");
          } else {
            samplingPeriod =
                new ComputeSubSamplingPeriod()
                    .setInputSize(inputWidth, inputHeight)
                    .setOutputSize(width, height)
                    .compute();
          }
          reader.getDefaultReadParam().setSourceSubsampling(samplingPeriod, samplingPeriod, 0, 0);
          BufferedImage image = null;
          image = reader.read(0);
          result.setOutput(Thumbnails.of(image).size(width, height).asBufferedImage());
        });
    return result.getOutput();
  }

  @Override
  public ImageSizer getNew() {
    return new SamplingSizer();
  }
}
