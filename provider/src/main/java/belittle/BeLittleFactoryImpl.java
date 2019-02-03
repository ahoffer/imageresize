package belittle;

import java.util.List;
import java.util.Map;

public class BeLittleFactoryImpl implements BeLittleFactory {

  Map<String, List<ImageSizer>> configuredSizers;

  public BeLittleFactoryImpl(Map<String, List<ImageSizer>> configuredSizers) {
    this.configuredSizers = configuredSizers;
  }

  @Override
  public BeLittle newBeLittler(BeLittleSizerSetting settings) {
    return new BeLittleImpl(configuredSizers, settings);
  }

  @Override
  public BeLittleSizerSetting newSettings() {
    return new BeLittleSizerSettingImpl();
  }
}
