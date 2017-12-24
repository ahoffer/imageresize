package com.github.ahoffer.sizeimage.provider;

import com.github.ahoffer.fuzzyfile.FuzzyFile;
import com.github.ahoffer.sizeimage.ImageSizer;

/**
 * This class adds a single field. The field holds an object that represents the executable the
 * sizer needs to invoke to perform its work.
 */
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
