package belittle;

import java.util.List;
import java.util.Map;

public class BeLittleFactoryImpl implements BeLittleFactory {

  Map<String, List<ImageSizer>> configuredSizers;
  BeLittleSizerSetting sizerSetting;

  public BeLittleFactoryImpl(
      BeLittleSizerSetting sizerSetting, Map<String, List<ImageSizer>> configuredSizers) {
    this.configuredSizers = configuredSizers;
    this.sizerSetting = sizerSetting;
  }

  @Override
  public BeLittle newBeLittle(BeLittleSizerSetting sizerSetting) {
    return new BeLittleImpl(configuredSizers, sizerSetting);
  }

  @Override
  public BeLittle newBeLittle() {
    return new BeLittleImpl(configuredSizers, sizerSetting);
  }

  @Override
  public BeLittleSizerSetting newSetting() {
    return new BeLittleSizerSettingImpl(sizerSetting);
  }
}
