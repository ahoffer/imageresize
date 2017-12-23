package com.github.ahoffer.sizeimage.support;

public class ComputeResolutionLevel extends Computation {

  public static final int FULL_RESOLUTION = 0;

  // Assume 5 decomposition levels. Resolution levels = decomp levels + 1
  private int maxResolutionlevels = 6;

  @Override
  public int compute() {
    double biggestRatio = getBiggestRatio();
    return (int)
        Math.max(FULL_RESOLUTION, Math.min(maxResolutionlevels, logBase2Int(biggestRatio)));
  }

  public ComputeResolutionLevel setMaxResolutionlevels(int maxResolutionlevels) {
    this.maxResolutionlevels = maxResolutionlevels;
    return this;
  }
}
