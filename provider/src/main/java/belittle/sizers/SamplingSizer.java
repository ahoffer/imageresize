package belittle.sizers;

import static belittle.support.MessageConstants.CANNOT_READ_WIDTH_AND_HEIGHT;

import belittle.BeLittleMessage.BeLittleSeverity;
import belittle.BeLittleMessageImpl;
import belittle.BeLittleResult;
import belittle.BeLittleSizerSetting;
import belittle.ImageSizer;
import belittle.support.ComputeSubSamplingPeriod;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import javax.imageio.ImageReader;
import net.coobird.thumbnailator.Thumbnails;

public class SamplingSizer extends AbstractImageSizer {

  public static final String SAMPLING_PERIOD_KEY = "samplingPeriod";

  private Integer samplingPeriod;
  private ImageReader reader;

  public SamplingSizer(BeLittleSizerSetting sizerSetting) {
    super(sizerSetting);
  }

  void prepare(InputStream inputStream) {}

  void cleanup() {
    if (reader != null) {
      reader.dispose();
    }
  }

  @Override
  public BeLittleResult resize(File file, String mimeType) {
    closeImageReader(reader);
    reader = getImageReaderByMIMEType(mimeType);
    doWithImageInputStream(
        file,
        (iis) -> {
          reader.setInput(iis);
          int inputHeight = reader.getHeight(0);
          int inputWidth = reader.getWidth(0);
          if (inputHeight <= 0 || inputWidth <= 0) {
            int defaultSamplingPeriod =
                Integer.valueOf(sizerSetting.getProperty(SAMPLING_PERIOD_KEY));
            if (defaultSamplingPeriod > 0) {
              samplingPeriod = defaultSamplingPeriod;
              addMessage(
                  new BeLittleMessageImpl(
                      CANNOT_READ_WIDTH_AND_HEIGHT,
                      BeLittleSeverity.WARNING,
                      String.format(
                          "Width and height of input image cannot be determined. Using configured sampling period of %s pixels",
                          samplingPeriod)));

            } else {
              addMessage(
                  new BeLittleMessageImpl(
                      CANNOT_READ_WIDTH_AND_HEIGHT,
                      BeLittleSeverity.ERROR,
                      "Width and/or height of input image cannot be determined and no default sampling period is value is configured. Sampling sizer needs these values to compute subsampling period."));
            }
          } else {
            samplingPeriod =
                new ComputeSubSamplingPeriod()
                    .setInputSize(inputWidth, inputHeight)
                    .setOutputSize(sizerSetting.getWidth(), sizerSetting.getHeight())
                    .compute();
          }
          reader.getDefaultReadParam().setSourceSubsampling(samplingPeriod, samplingPeriod, 0, 0);

          BufferedImage image = null;

          image = reader.read(0);
          result.setOutput(
              Thumbnails.of(image)
                  .size(sizerSetting.getWidth(), sizerSetting.getHeight())
                  .asBufferedImage());
        });
    return result;
  }

  @Override
  public ImageSizer getNew(BeLittleSizerSetting sizerSetting) {
    return new SamplingSizer(sizerSetting);
  }

  @Override
  public BeLittleResult resize(File file) {
    throw new UnsupportedOperationException(
        "The sampling sizer expects a MIMME type. Use another implementation of the resize method");
  }
}
