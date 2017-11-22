package com.github.ahoffer.sizeimage.provider;

public abstract class Computation {
  public Computation() {
    outputHeight = 256;
    outputWidth = 256;
  }

  public static final double LOG_OF_2 = Math.log(2);
  int inputWidth;
  int inputHeight;
  int outputWidth;
  int outputHeight;

  public Computation setInputWidthHeight(int inputWidth, int inputHeight) {
    this.inputWidth = inputWidth;
    this.inputHeight = inputHeight;
    return this;
  }

  public Computation setOutputWidthHeight(int outputWidth, int outputHeight) {
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
    return Math.round(logBase2(number));
  }

  public abstract int compute();

  protected double getBiggestRatio() {
    double widthRatio = inputWidth / (double) outputWidth;
    double heightRatio = inputHeight / (double) outputHeight;
    return Math.max(widthRatio, heightRatio);
  }
}
