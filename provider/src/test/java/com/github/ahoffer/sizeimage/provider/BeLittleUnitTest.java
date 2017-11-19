package com.github.ahoffer.sizeimage.provider;

import static com.github.ahoffer.sizeimage.provider.BeLittle.MATCH_ANY;
import static org.hamcrest.CoreMatchers.any;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.github.ahoffer.sizeimage.ImageSizer;
import com.github.ahoffer.sizeimage.provider.BeLittle.ImageSizerCollection;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class BeLittleUnitTest {

  public static final String MANY = "many";
  public static final String NONE = "none";
  private BeLittle belittle;
  private ImageSizer wildcardSizer;
  private ImageSizer basicSizer;
  private ImageSizer otherSizer;
  private ImageSizer unavailableSizer;
  private ImageSizer duplicatedInstance;
  private List<ImageSizer> wildcardList;
  private List<ImageSizer> multiplesList;

  @Before
  public void setUp() throws Exception {
    belittle = new BeLittle();
    wildcardSizer = mock(SamplingSizer.class);

    basicSizer = mock(BasicSizer.class);
    otherSizer = mock(ImageSizer.class);
    unavailableSizer = mock(ImageSizer.class);
    duplicatedInstance = otherSizer;

    doReturn(true).when(wildcardSizer).isAvailable();
    doReturn(true).when(basicSizer).isAvailable();
    doReturn(true).when(otherSizer).isAvailable();
    doReturn(false).when(unavailableSizer).isAvailable();

    doReturn(wildcardSizer).when(wildcardSizer).getNew();
    //    doReturn(magikSizer).when(magikSizer).isAvailable();
    doReturn(basicSizer).when(basicSizer).getNew();
    doReturn(otherSizer).when(otherSizer).getNew();
    doReturn(unavailableSizer).when(unavailableSizer).getNew();

    belittle.setConfiguration(getTestConfiguration());
  }

  private Map<String, List<ImageSizer>> getTestConfiguration() {
    Map<String, List<ImageSizer>> configuration = new HashMap<>();
    wildcardList = Arrays.asList(wildcardSizer);
    configuration.put(MATCH_ANY, wildcardList);
    List<ImageSizer> singletonList = Arrays.asList(otherSizer);
    multiplesList = Arrays.asList(basicSizer, unavailableSizer, duplicatedInstance, otherSizer);
    configuration.put(MANY, multiplesList);
    return configuration;
  }

  @Ignore
  @Test
  public void testRecomendations() throws Exception {

    List<ImageSizer> sizers = belittle.getSizersFor(MANY).getRecommendations();
    // (This does not work because of mocks) --> assertThat(sizers, contains(multiplesList));
    // --Test ordering--
    assertThat(sizers.get(0), is(multiplesList.get(0)));
    assertThat(sizers.get(sizers.size() - 1), is(multiplesList.get(multiplesList.size() - 1)));
  }

  @Test
  public void testAll() {
    ImageSizerCollection coll = belittle.getSizersFor(NONE);
    Set<ImageSizer> sizers = coll.getAll();

    assertThat(
        "Order does not matter",
        sizers,
        containsInAnyOrder(basicSizer, unavailableSizer, wildcardSizer, otherSizer));
    assertThat(
        "Duplicate instances should be filtered out, but unavailable instance should not",
        coll.getAll(),
        hasSize(4));
  }

  @Test
  public void testRecommended() {
    ImageSizerCollection coll = belittle.getSizersFor(MANY);
    assertThat(
        "Expected a recommended sizer for this configuration",
        coll.getRecommended().isPresent(),
        is(true));
    assertThat("Expected a different sizer", coll.getRecommended().get(), is(multiplesList.get(0)));
  }

  @Test
  public void configurationIsNull() {
    belittle.setConfiguration(null);
    ImageSizerCollection coll = belittle.getSizersFor(NONE);
    assertThat(
        "There should be no sizers if there is no configuration",
        coll.getRecommended().isPresent(),
        is(false));
    assertThat("Should default to zero", belittle.getMaxHeight(), is(0));
    assertThat("Should default to zero", belittle.getMaxWidth(), is(0));
    assertThat("Configuration should be empty map", belittle.getConfiguration(), any(Map.class));
  }

  @Test
  public void testWildcards() {
    ImageSizerCollection coll = belittle.getSizersFor((String) null);
    assertThat("Unexpected number of sizers", coll.getWildcards(), hasSize(wildcardList.size()));
    assertThat(
        "Null or unknown MIME type should return wildcard sizers",
        coll.getRecommendations(),
        hasSize(wildcardList.size()));
    assertThat("Wrong sizer", coll.getWildcards(), hasItem(wildcardSizer));
    assertThat(
        "Null or unknown MIME type should return wildcard sizers",
        coll.getRecommendations(),
        hasItem(wildcardSizer));
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testUnmodifiableList() {
    belittle.getConfiguration().get(MANY).add(mock(ImageSizer.class));
  }
}
