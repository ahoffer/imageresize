package belittle.sizers;

import belittle.BeLittleSizerSetting;
import belittle.support.FuzzyFile;

/**
 * This class adds a single field. The field holds an object that represents the executable the
 * sizer needs to invoke to perform its work.
 */
public abstract class ExternalProcessSizer extends AbstractImageSizer {

  FuzzyFile executable;

  public ExternalProcessSizer(BeLittleSizerSetting sizerSettings) {
    super(sizerSettings);
  }

  public FuzzyFile getExecutable() {
    return executable;
  }

  public void setExecutable(FuzzyFile executable) {
    this.executable = executable;
  }

  @Override
  public boolean isAvailable() {
    return getExecutable().canExecute();
  }
}
