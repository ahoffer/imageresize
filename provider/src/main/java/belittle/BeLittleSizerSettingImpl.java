package belittle;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

// CLASS HAS MUTABLE STATE. But copies objects passed into it.

public class BeLittleSizerSettingImpl implements BeLittleSizerSetting {

  private Map<String, String> sizerPropertiesMap = new HashMap<>();

  public BeLittleSizerSettingImpl() {
    // Set reasonable defaults
    setTimeoutSeconds(BeLittleConstants.DEFAULT_TIMEOUT_SECONDS);
    setWidth(256);
    setHeight(256);
  }

  // Uses default values for missing keys.
  // Adds new keys or overwrites exiting keys.
  public BeLittleSizerSettingImpl(Map<String, String> sizerPropertiesMap) {
    this();
    this.sizerPropertiesMap.putAll(sizerPropertiesMap);
  }

  // Copy constructor
  public BeLittleSizerSettingImpl(BeLittleSizerSetting sizerSetting) {
    this.sizerPropertiesMap = new HashMap<>(sizerSetting.getProperties());
  }

  @Override
  public BeLittleSizerSetting setProperty(String name, String value) {
    sizerPropertiesMap.put(name, value);
    return this;
  }

  @Override
  public String getProperty(String name) {
    return sizerPropertiesMap.get(name);
  }

  @Override
  public int getTimeoutSeconds() {
    return Integer.valueOf(sizerPropertiesMap.get(BeLittleConstants.TIMEOUT_SECONDS));
  }

  @Override
  public void setTimeoutSeconds(int seconds) {
    setProperty(BeLittleConstants.TIMEOUT_SECONDS, String.valueOf(seconds));
  }

  @Override
  public int getWidth() {
    return Integer.valueOf(sizerPropertiesMap.get(BeLittleConstants.MAX_WIDTH));
  }

  @Override
  public void setWidth(int width) {
    setProperty(BeLittleConstants.MAX_WIDTH, String.valueOf(width));
  }

  @Override
  public int getHeight() {
    return Integer.valueOf(sizerPropertiesMap.get(BeLittleConstants.MAX_HEIGHT));
  }

  @Override
  public void setHeight(int height) {
    setProperty(BeLittleConstants.MAX_HEIGHT, String.valueOf(height));
  }

  @Override
  public Map<String, String> getProperties() {
    return Collections.unmodifiableMap(sizerPropertiesMap);
  }
}
