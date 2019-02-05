package belittle.sizers;

import static belittle.support.MessageConstants.COULD_NOT_READ_METADATA;
import static belittle.support.MessageConstants.REDUCTION_FACTOR;
import static belittle.support.MessageConstants.SAMPLE_PERIOD;

import belittle.BeLittleResult;
import belittle.BeLittleSizerSetting;
import belittle.ImageSizer;
import belittle.support.ComputeResolutionLevel;
import belittle.support.ComputeSubSamplingPeriod;
import belittle.support.Jpeg2000MetadataMicroReader;
import com.github.jaiimageio.jpeg2000.impl.J2KImageReadParamJava;
import com.github.jaiimageio.jpeg2000.impl.J2KImageReader;
import com.github.jaiimageio.jpeg2000.impl.J2KImageReaderSpi;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageReader;
import javax.imageio.spi.IIORegistry;

@SuppressWarnings("squid:S2160")
public class JaiJpeg2000Sizer extends AbstractImageSizer {

  /*

  // TODO: Make this configurable.
    TODO: Do not set bit per pixel for images close to the target size, or else there will be too little
    information and the output image will be very blurry
  */
  public static final double DEFAULT_BITS_PER_PIXEL = 0.3;

  static {
    IIORegistry.getDefaultInstance().registerServiceProvider(new J2KImageReaderSpi());
  }

  Jpeg2000MetadataMicroReader metadata;

  public JaiJpeg2000Sizer(BeLittleSizerSetting sizerSetting) {
    super(sizerSetting);
  }

  void readMetadata(File file) {
    // TODO: ortho-744mb.jp2 comes back with a reduction factor of 0? Is that really how the thing
    // is encoded or is there something wrong with the metadata reader?
    // UPDATE: There is no dimension information in the header or codestream boxes.
    doWithInputStream(
        file,
        (istream) -> {
          metadata = new Jpeg2000MetadataMicroReader(istream);
          metadata.read();
        });

    if (!metadata.isSucessfullyRead()) {
      addMessage(messageFactory.make(COULD_NOT_READ_METADATA));
    }
  }

  int getReductionFactor() {
    return new ComputeResolutionLevel()
        .setMaxResolutionLevels(metadata.getMinNumResolutionLevels())
        .setInputSize(metadata.getWidth(), metadata.getHeight())
        .setOutputSize(sizerSetting.getWidth(), sizerSetting.getHeight())
        .compute();
  }

  @Override
  public BeLittleResult resize(File file) {
    readMetadata(file);
    J2KImageReadParamJava param = new J2KImageReadParamJava();
    int reductionFactor = getReductionFactor();
    param.setResolution(reductionFactor);
    addMessage(messageFactory.make(REDUCTION_FACTOR, reductionFactor));
    param.setDecodingRate(DEFAULT_BITS_PER_PIXEL);
    doWithImageInputStream(
        file,
        (iis) -> {
          ImageReader reader = null;
          try {
            reader = new J2KImageReader(null);
            reader.setInput(iis);
            // TODO: this sampling part needs testing.
            if (reductionFactor == 0) {
              int samplingPeriod = getSamplingPeriod(reader.getWidth(0), reader.getWidth(0));
              param.setSourceSubsampling(samplingPeriod, samplingPeriod, 0, 0);
              addMessage(messageFactory.make(SAMPLE_PERIOD, samplingPeriod));
            }
            BufferedImage decodedImage = reader.read(0, param);
            result.setOutput(decodedImage);
          } finally {
            closeImageReader(reader);
          }
        });
    return result;
  }

  private int getSamplingPeriod(int width, int height) {
    return new ComputeSubSamplingPeriod()
        .setInputSize(width, height)
        .setOutputSize(sizerSetting.getWidth(), sizerSetting.getWidth())
        .compute();
  }

  @Override
  public ImageSizer getNew(BeLittleSizerSetting sizerSetting) {
    return new JaiJpeg2000Sizer(sizerSetting);
  }
}
