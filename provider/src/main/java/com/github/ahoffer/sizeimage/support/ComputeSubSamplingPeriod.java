package com.github.ahoffer.sizeimage.support;

public class ComputeSubSamplingPeriod extends Computation {

  public static final int NO_SUBSAMPLING = 1;

  public int compute() {
    double biggestRatio = getBiggestRatio();
    // The cast to (int) drops the decimal, effectively getting the floor of a positive integer.
    int subsamplingPeriod = (int) Math.max(NO_SUBSAMPLING, Math.round(biggestRatio));
    return subsamplingPeriod;
  }
}
