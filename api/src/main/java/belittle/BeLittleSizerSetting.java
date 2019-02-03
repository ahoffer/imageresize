package belittle;

import java.util.Map;

public interface BeLittleSizerSetting {

  BeLittleSizerSetting setProperty(String name, String value);

  String getProperty(String name);

  int getTimeoutSeconds();

  BeLittleSizerSetting setTimeoutSeconds(int seconds);

  int getWidth();

  BeLittleSizerSetting setWidth(int width);

  int getHeight();

  BeLittleSizerSetting setHeight(int height);

  Map<String, String> getProperties();
}
