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

  public void setExecutable(FuzzyFile executable) {
    this.executable = executable;
  }

  @Override
  public ImageSizer getNew() {
    ImageSizer newInstance = super.getNew();
    ((ExternalProcessSizer) newInstance).setExecutable(getExecutable());
    return newInstance;
  }
}
