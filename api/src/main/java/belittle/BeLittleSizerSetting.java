package belittle;

import java.util.Map;

public interface BeLittleSizerSetting {

  BeLittleSizerSetting setProperty(String name, String value);

  String getProperty(String name);

  int getTimeoutSeconds();

  void setTimeoutSeconds(int seconds);

  int getWidth();

  void setWidth(int width);

  int getHeight();

  void setHeight(int height);

  Map<String, String> getProperties();
}
