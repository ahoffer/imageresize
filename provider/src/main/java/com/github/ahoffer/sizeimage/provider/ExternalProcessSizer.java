package com.github.ahoffer.sizeimage.provider;

import com.github.ahoffer.fuzzyfile.FuzzyFile;
import com.github.ahoffer.sizeimage.ImageSizer;

public abstract class ExternalProcessSizer extends AbstractImageSizer {

  FuzzyFile executable;

  public FuzzyFile getExecutable() {
    return executable;
  }

  public ImageSizer setExecutable(FuzzyFile executable) {
    this.executable = executable;
    return this;
  }
}
