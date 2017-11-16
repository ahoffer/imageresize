package com.github.ahoffer.sizeimage.provider;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;

public class SamplingImageReader {
  protected int samplePeriod;

  int imageIndex;

  ImageReader reader;

  InputStream source;

  int subsamplingHint = 512;

  public static SamplingImageReader of(InputStream source) throws IOException {
    SamplingImageReader object = new SamplingImageReader();
    object.source = source;
    object.reader = new BeLittleHelper().getReader(source);
    return object;
  }

  public static SamplingImageReader of(File sourceFile) throws IOException {
    SamplingImageReader object = new SamplingImageReader();
    object.source = new FileInputStream(sourceFile);
    object.reader = new BeLittleHelper().getReader(new FileInputStream(sourceFile));
    return object;
  }

  public SamplingImageReader subsamplingHint(int hint) {
    subsamplingHint = hint;
    return this;
  }

  public SamplingImageReader imageIndex(int index) {
    imageIndex = index;
    return this;
  }

  public SamplingImageReader samplePeriod(int period) {
    samplePeriod = period;
    return this;
  }

  public int computeSamplingPeriod() {
    if (samplePeriod == 0) {
      try {
        int longestDimensionSize =
            Math.max(reader.getWidth(imageIndex), reader.getHeight(imageIndex));
        samplePeriod =
            (int) (Math.round(Math.ceil(longestDimensionSize / (double) subsamplingHint)));

      } catch (IOException e) {
        // Give up. Do not sub-sample the image.
        samplePeriod = 1;
      }
    }
    return samplePeriod;
  }

  public BufferedImage read() throws IOException {
    BufferedImage image;
    int columnOffset = 0;
    int rowOffset = 0;
    // Use the same sampling period for both rows and columns to preserve images's
    // aspect ratio.
    int columnSamplingPeriod = computeSamplingPeriod();
    int rowSamplingPeriod = computeSamplingPeriod();
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
}
