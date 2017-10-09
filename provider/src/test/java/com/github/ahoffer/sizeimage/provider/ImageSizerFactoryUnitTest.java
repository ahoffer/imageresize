package com.github.ahoffer.sizeimage.provider;

import com.github.ahoffer.sizeimage.ImageSizer;
import static com.github.ahoffer.sizeimage.provider.ImageSizerFactory.MATCH_ANY;
import java.util.*;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class ImageSizerFactoryUnitTest {

  public static final String EMPTY = "empty";
  public static final String SINGLE = "single";
  public static final String MULTIPLE = "multiple";
  private ImageSizerFactory factory;
  private ImageSizer imageSizer1;
  private ImageSizer imageSizer2;
  private ImageSizer imageSizer3;
  private List<ImageSizer> defaultList;
  private List<ImageSizer> emptyList;
  private List<ImageSizer> singletonList;
  private List<ImageSizer> multiplesList;

  @Before
  public void setUp() throws Exception {
    imageSizer1 = mock(SamplingSizer.class);
    imageSizer2 = mock(MagickSizer.class);
    imageSizer3 = mock(BasicSizer.class);
    SamplingSizer imageSizer11 = mock(SamplingSizer.class);
    MagickSizer imageSizer22 = mock(MagickSizer.class);
    BasicSizer imageSizer33 = mock(BasicSizer.class);
    doReturn(true).when(imageSizer1).isAvailable();
    doReturn(true).when(imageSizer11).isAvailable();
    doReturn(true).when(imageSizer2).isAvailable();
    doReturn(true).when(imageSizer22).isAvailable();
    doReturn(true).when(imageSizer3).isAvailable();
    doReturn(true).when(imageSizer33).isAvailable();
    doReturn(imageSizer11).when(imageSizer1).getNew();
    doReturn(imageSizer22).when(imageSizer2).getNew();
    doReturn(imageSizer33).when(imageSizer3).getNew();
    factory = new ImageSizerFactory();

    Map<String, List<ImageSizer>> configuration = new HashMap<>();
    defaultList = Arrays.asList(imageSizer1);
    configuration.put(MATCH_ANY, defaultList);
    emptyList = Arrays.asList();
    configuration.put(EMPTY, emptyList);
    singletonList = Arrays.asList(imageSizer1);
    configuration.put(SINGLE, singletonList);
    multiplesList = Arrays.asList(imageSizer1, imageSizer2, imageSizer3);
    configuration.put(MULTIPLE, multiplesList);
    factory.setConfiguration(configuration);
  }

  @Test
  public void testDefaultSizers() throws Exception {
    List<ImageSizer> sizers = factory.getWildcardSizers();
    assertThat(sizers.size(), is(1));
    assertThat(sizers, contains(defaultList.get(0)));
  }

  @Test
  public void testConfigurationForEmptyList() {
    List<ImageSizer> sizers = factory.getRecommendedSizers("empty");
    assertThat("Expected empty list", sizers, empty());
  }

  @Test
  public void testReturnOrder() throws Exception {
    List<ImageSizer> sizers = factory.getRecommendedSizers(MULTIPLE);
    assertThat(sizers, hasSize(multiplesList.size()));
    assertThat(sizers.get(0), instanceOf(SamplingSizer.class));
    assertThat(sizers.get(1), instanceOf(MagickSizer.class));
    assertThat(sizers.get(2), instanceOf(BasicSizer.class));
  }

  @Test
  public void testInitializaiton() {
    assertThat(
        "Configuration should be empty map",
        new ImageSizerFactory().getConfiguration().isEmpty(),
        is(true));
  }

  @Test
  public void testPrototypeBehavior() {
    List<ImageSizer> sizers = factory.getRecommendedSizers(SINGLE);
    assertThat(sizers, not(empty()));
    assertThat("Wrong kind of object", sizers.get(0), instanceOf(ImageSizer.class));
    assertThat(sizers.get(0), not(singletonList.get(0)));
  }

  @Test
  public void configurationIsNull() {
    factory.setConfiguration(null);
    assertThat(factory.getRecommendedSizer(MULTIPLE).isPresent(), is(false));
  }

  @Test
  public void testNullMimeType() {
    assertThat(factory.getRecommendedSizers((String) null), hasItem(instanceOf(ImageSizer.class)));
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testUnmodifiableList() {
    factory.getConfiguration().get(MATCH_ANY).add(imageSizer2);
  }

  @Test
  public void testNullMimeTypeWithAvailability() {
    ImageSizer mockImageSizer;

    mockImageSizer = mock(ImageSizer.class);
    setReturnValueForIsAvailable(mockImageSizer, true);
    assertThat(factory.getRecommendedSizer((String) null).isPresent(), is(true));

    mockImageSizer = mock(ImageSizer.class);
    setReturnValueForIsAvailable(mockImageSizer, false);
    assertThat(factory.getRecommendedSizer((String) null).isPresent(), is(false));
  }

  @Test
  public void testBooleanFalseWithNoDefault() {
    factory.setConfiguration(null);
    List<ImageSizer> list = factory.getRecommendedSizers((String) null, false);
    assertThat(list, empty());
  }

  @Test
  public void testBooleanFalseWithDefaults() {
    ImageSizer mockImageSizer = mock(ImageSizer.class);
    setReturnValueForIsAvailable(mockImageSizer, false);
    assertThat(
        factory.getRecommendedSizers(MATCH_ANY, false), everyItem(instanceOf(ImageSizer.class)));

    setReturnValueForIsAvailable(mockImageSizer, false);
    assertThat(factory.getRecommendedSizers(MATCH_ANY, true), empty());
  }

  private void setReturnValueForIsAvailable(ImageSizer mockImageSizer, boolean returnValue) {
    ImageSizer clone = mock(ImageSizer.class);

    // Set return value for both mocks because the return value of the availability method
    // should not change if the real object is original or a clone of itself
    doReturn(returnValue).when(clone).isAvailable();
    doReturn(returnValue).when(mockImageSizer).isAvailable();
    doReturn(clone).when(mockImageSizer).getNew();
    HashMap configuration = new HashMap();
    configuration.put(MATCH_ANY, Arrays.asList(mockImageSizer));
    factory.setConfiguration(configuration);
  }
}
