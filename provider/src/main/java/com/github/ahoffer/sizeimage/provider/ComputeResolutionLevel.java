package com.github.ahoffer.sizeimage.provider;

public class ComputeResolutionLevel extends Computation {

  public static final int FULL_RESOLUTION = 0;
  /**
   * Diego Santa Cruz, Touradj Ebrahimi, and Charilaos Christopoulos
   *
   * <p>Dr. Dobb's Journal, April 01, 2001
   *
   * <p>... by reconstructing only some of the decomposition levels, a lower resolution version of
   * the image can be obtained. Each of these resolutions is called a "resolution level." Typically,
   * five decomposition levels are used, which results in six resolution levels, all related by a
   * factor of two." End of quotation.
   *
   * <p>For example:
   *
   * <p>Original image size = 4904 x 5951
   *
   * <p>Decoding resolution level 1 = 2452 x 2976 image
   *
   * <p>Decoding resolution level 2 = 1226 x 1488 image
   *
   * <p>Decoding resolution level 3 = 613 x 744 image
   */

  // Assume 5 decomposition levels. Resolution levels = decomp levels + 1
  int assumedMaximumNumberOfResolutionlevels = 6;

  @Override
  public int compute() {
    double biggestRatio = getBiggestRatio();
    return (int)
        Math.max(
            FULL_RESOLUTION,
            Math.min(assumedMaximumNumberOfResolutionlevels, logBase2Int(biggestRatio)));
  }
}
