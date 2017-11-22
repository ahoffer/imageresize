package com.github.ahoffer.sizeimage.provider;

class ComputeSubSamplingPeriod extends Computation {

  public static final int NO_SUBSAMPLING = 1;

  public int compute() {
    double biggestRatio = getBiggestRatio();
    int subsamplingPeriod = (int) Math.max(NO_SUBSAMPLING, Math.round(biggestRatio));
    return subsamplingPeriod;
  }
}
