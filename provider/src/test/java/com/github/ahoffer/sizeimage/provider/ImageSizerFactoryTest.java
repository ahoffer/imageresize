package com.github.ahoffer.sizeimage.provider;

import com.github.ahoffer.sizeimage.ImageSizer;
import java.util.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.hamcrest.core.IsNot.not;
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
  public void getDefaultSizers() throws Exception {
    Optional<List<ImageSizer>> sizers = factory.getDefaultSizers();
    assertThat("Expected sizer to exist", sizers.isPresent(), is(true));
    isDefaultList(sizers.get());
  }

  @Test
  public void configurationForEmptyList() {
    List<ImageSizer> sizers = factory.getRecommendedSizers("empty");
    assertThat("Expected empty list", sizers.isEmpty(), is(true));
  }

  @Test
  public void configurationMissingDefaults() {
    Map<String, List<ImageSizer>> configuration = getTestConfiguration();
    configuration.remove(ImageSizerFactory.MATCH_ANY);
    factory.setConfiguration(configuration);
  }

  @Test
  public void testReturnOrder() throws Exception {
    List<ImageSizer> sizers = factory.getRecommendedSizers("multiple");
    isDefaultList(sizers);
  }

  @Test
  public void configurationIsNull1() {
    // todo
    factory.setConfiguration(null);
    factory.getDefaultSizers();
  }

  @Test
  public void testPrototypeBehavior() {
    List<ImageSizer> sizers1 = factory.getRecommendedSizers("single");
    List<ImageSizer> sizers2 = factory.getRecommendedSizers("single");
    assertThat(sizers1.get(0), not(sameInstance(sizers2.get(0))));
  }

  @Test
  public void configurationIsNull2() {
    // todo
    factory.setConfiguration(null);
    factory.getRecommendedSizer("multiple");
  }

  @Test(expected = RuntimeException.class)
  public void configurationIsNull3() {
    factory.setConfiguration(null);
    factory.getRecommendedSizer("multiple");
  }

  @Test
  public void testNullMimeType1() {
    isDefaultList(factory.getRecommendedSizers(null));
  }

  @Test
  public void testNullMimeType2() {
    // todo
    factory.getRecommendedSizer(null);
  }

  @Test
  public void testConfiguration() throws Exception {}

  Map<String, List<ImageSizer>> getTestConfiguration() {
    Map<String, List<ImageSizer>> configuration = new HashMap<>();
    configuration.put(ImageSizerFactory.MATCH_ANY, Arrays.asList(new BasicImageSizer()));
    configuration.put("empty", Arrays.asList());
    configuration.put("single", Arrays.asList(new SamplingImageSizer()));
    configuration.put(
        "multiple",
        Arrays.asList(new ImageMagickSizer(), new SamplingImageSizer(), new BasicImageSizer()));
    return configuration;
  }

  private void isDefaultList(List<ImageSizer> sizers) {
    assertThat(sizers.size(), is(3));
    assertThat(sizers.get(0), instanceOf(ImageMagickSizer.class));
    assertThat(sizers.get(1), instanceOf(SamplingImageSizer.class));
    assertThat(sizers.get(2), instanceOf(BasicImageSizer.class));
  }
}
