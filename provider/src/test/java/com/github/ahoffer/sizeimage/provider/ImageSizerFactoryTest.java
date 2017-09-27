package com.github.ahoffer.sizeimage.provider;

import com.github.ahoffer.sizeimage.ImageSizer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;

public class ImageSizerFactoryTest {
  ImageSizerFactory factory;

  @Before
  public void setUp() throws Exception {
    factory = new ImageSizerFactory();
    factory.setConfiguration(getTestConfiguration());
  }

  @Test
  public void getDefaultSizers() throws Exception {}

  @Test
  public void configurationForEmptyList() {}

  @Test
  public void configurationMissingDefaults() {}

  @Test
  public void testReturnOrder() throws Exception {}

  @Test
  public void testNullMimeType() {}

  @Test
  public void testConfiguration() throws Exception {}

  Map<String, List<ImageSizer>> getTestConfiguration() {
    Map<String, List<ImageSizer>> configuration = new HashMap<>();
    configuration.put(ImageSizerFactory.MATCH_ANY, Arrays.asList(new BasicImageSizer()));
    configuration.put("empty", Arrays.asList());
    configuration.put(
        "multiple",
        Arrays.asList(new ImageMagickSizer(), new SamplingImageSizer(), new BasicImageSizer()));
    return configuration;
  }
}
