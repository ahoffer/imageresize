package com.github.ahoffer.sizeimage.provider;

class ComputeResizeFactor {

  int inputWidth;
  int inputHeight;
  int hint;

  public ComputeResizeFactor() {
    hint = 512;
  }

  @SuppressWarnings("unused")
  public ComputeResizeFactor(int hint) {
    this.hint = hint;
  }

  public ComputeResizeFactor setWidthHeight(int width, int height) {
    inputWidth = width;
    inputHeight = height;
    return this;
  }

  public int compute() {
    int longestDimensionSize = Math.max(inputWidth, inputHeight);
    return (int) (Math.round(Math.ceil(longestDimensionSize / (double) hint)));
  }
}
