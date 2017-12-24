package com.github.ahoffer.sizeimage.support;

/**
 * A cacluation to determine how many resolution levels should be loaded from a JPEG 2000 image. At
 * level 0, the image is decompressed at its fullest size. Each level above 0 reduces the width and
 * height by a factor of 2. TODO: The default number of decomposition layers for JPEG 2000
 * compressors is 5. If the actual number of decomposition layers cannot be determined from the
 * image metadata, the image is assumed to have NO decomposition layers. A more aggressive strategy
 * would be to assume the image has 5 decompostion layers. So long as the assumptions holds,
 * reducing the size of JPEG 2000 image missing metadata would be much faster.
 */
public class ComputeResolutionLevel extends Computation {

  public static final int FULL_RESOLUTION = 0;

  // Assume 5 decomposition levels. Resolution levels = decomp levels + 1
  private int maxResolutionLevels = 6;

  @Override
  public int compute() {
    double biggestRatio = getBiggestRatio();
    return (int)
        Math.max(FULL_RESOLUTION, Math.min(maxResolutionLevels, logBase2Int(biggestRatio)));
  }

  public ComputeResolutionLevel setMaxResolutionLevels(int maxResolutionLevels) {
    this.maxResolutionLevels = maxResolutionLevels;
    return this;
  }
}
