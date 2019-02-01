package com.github.ahoffer.sizeimage.sizers;

import com.github.ahoffer.sizeimage.BeLittleSizerSetting;
import com.github.ahoffer.sizeimage.BeLittlingResult;
import com.github.ahoffer.sizeimage.support.FuzzyFile;

/**
 * This class adds a single field. The field holds an object that represents the executable the
 * sizer needs to invoke to perform its work.
 */
public abstract class ExternalProcessSizer extends AbstractImageSizer {

  FuzzyFile executable;

  public ExternalProcessSizer(BeLittleSizerSetting sizerSettings, BeLittlingResult result) {
    super(sizerSettings, result);
  }

  public FuzzyFile getExecutable() {
    return executable;
  }

  public void setExecutable(FuzzyFile executable) {
    this.executable = executable;
  }
}
