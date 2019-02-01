package com.github.ahoffer.sizeimage;

public interface BeLittleFactory {

  // this class exists just to avoid threading issues.

  public BeLittle newBeLittler(BeLittleSizerSetting settings);

  BeLittleSizerSetting newSettings();
}
