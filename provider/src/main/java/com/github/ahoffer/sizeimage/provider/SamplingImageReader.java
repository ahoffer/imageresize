package com.github.ahoffer.sizeimage.provider;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;

public class SamplingImageReader {

  public static final int NO_SUBSAMPLING = 1;
  static ImageReaderShortcuts shortcuts = new ImageReaderShortcuts();
  private int samplingPeriod;
  int imageIndex;
  ImageReader reader;
  InputStream source;

  public static SamplingImageReader of(InputStream source) throws IOException {
    SamplingImageReader object = new SamplingImageReader();
    object.source = source;
    object.reader = shortcuts.getReader(source);
    return object;
  }

  @SuppressWarnings("unused")
  public static SamplingImageReader of(File sourceFile) throws IOException {
    SamplingImageReader object = new SamplingImageReader();
    object.source = new FileInputStream(sourceFile);
    object.reader = shortcuts.getReader(new FileInputStream(sourceFile));
    return object;
  }

  public SamplingImageReader imageIndex(int index) {
    imageIndex = index;
    return this;
  }

  // TODO: RE-ENABLE OVERWRITE OF COMPUTED SAMPLE PERIOD
  public SamplingImageReader samplePeriod(int period) {
    samplingPeriod = period;
    return this;
  }

  public int computeSamplingPeriod() {
    int period;
    int sourceWidth = 0;
    int sourceHeight = 0;
    try {
      sourceWidth = reader.getWidth(imageIndex);
      sourceHeight = reader.getHeight(imageIndex);
      period = new ComputeResizeFactor().setWidthHeight(sourceWidth, sourceHeight).compute();
    } catch (IOException e) {
      period = NO_SUBSAMPLING;
    }
    return period;
  }

  public BufferedImage read() throws IOException {
    BufferedImage image;
    int columnOffset = 0;
    int rowOffset = 0;
    // Use the same sampling period for both rows and columns to preserve images's
    // aspect ratio.
    samplingPeriod = computeSamplingPeriod();
    int columnSamplingPeriod = getSamplingPeriod();
    int rowSamplingPeriod = getSamplingPeriod();
    ImageReadParam imageParam = reader.getDefaultReadParam();
    try {
      imageParam.setSourceSubsampling(
          columnSamplingPeriod, rowSamplingPeriod, columnOffset, rowOffset);
      image = reader.read(imageIndex, imageParam);
    } finally {
      source.close();
      reader.dispose();
    }
    return image;
  }

  protected int getSamplingPeriod() {
    return samplingPeriod;
  }
}
