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
  static ImageReaderShortcuts shortcuts = new ImageReaderShortcuts();

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

  public SamplingImageReader samplePeriod(int period) {
    samplePeriod = period;
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
      period = 1;
    }
    return period;
  }

  public BufferedImage read() throws IOException {
    BufferedImage image;
    int columnOffset = 0;
    int rowOffset = 0;
    // Use the same sampling period for both rows and columns to preserve images's
    // aspect ratio.
    int period = computeSamplingPeriod();
    int columnSamplingPeriod = period;
    int rowSamplingPeriod = period;
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
