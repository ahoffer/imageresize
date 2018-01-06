package com.github.ahoffer.sizeimage.support;

/**
 * This class represents some calculation needed to resize and image. It exists to remove duplicate
 * code and to separate out the logic of the calculation away from the ImageSizer class. At the
 * abstract level, it knows the width and height of the input image and the desired (maximum) width
 * and (maximum) height of the output image. Subclasses use this information as part of a specific
 * calculation needed by one or more image sizer.
 */
public abstract class Computation {
  public static final double LOG_OF_2 = Math.log(2);
  int inputWidth;
  int inputHeight;
  int outputWidth;
  int outputHeight;

  public Computation() {
    outputHeight = 256;
    outputWidth = 256;
  }

  public Computation setInputSize(int inputWidth, int inputHeight) {
    this.inputWidth = inputWidth;
    this.inputHeight = inputHeight;
    return this;
  }

  public Computation setOutputSize(int outputWidth, int outputHeight) {
    this.outputWidth = outputWidth;
    this.outputHeight = outputHeight;
    return this;
  }

  protected double logBase2(double number) {

    Double logOfNumber = Math.log(number);
    if (logOfNumber.isInfinite() || logOfNumber.isNaN()) {
      return Double.NaN;
    }
    return logOfNumber / LOG_OF_2;
  }

  protected long logBase2Int(double number) {
    return Math.round(Math.floor(logBase2(number)));
  }

  public abstract int compute();

  protected double getBiggestRatio() {
    double widthRatio = inputWidth / (double) outputWidth;
    double heightRatio = inputHeight / (double) outputHeight;
    return Math.max(widthRatio, heightRatio);
  }
}
