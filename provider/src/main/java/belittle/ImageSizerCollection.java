package belittle;

import static belittle.BeLittleConstants.UNKNOWN_MIME_TYPE;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class ImageSizerCollection {

  Map<String, List<ImageSizer>> sizerMap;

  public ImageSizerCollection(Map<String, List<ImageSizer>> sizerMap) {
    this.sizerMap = copySizerMap(sizerMap);
  }

  public ImageSizerCollection(ImageSizerCollection coll) {
    this(coll.sizerMap);
  }

  private Map<String, List<ImageSizer>> copySizerMap(Map<String, List<ImageSizer>> sizers) {
    Map<String, List<ImageSizer>> copy = new HashMap<>(sizers);
    // Copy the sizer instances
    copy.replaceAll((key, list) -> copySizerList(list));
    return copy;
  }

  List<ImageSizer> copySizerList(List<ImageSizer> sizerList) {
    return sizerList.stream().map(sizer -> sizer.getNew()).collect(Collectors.toList());
  }

  public List<ImageSizer> getSizersForMimeType(String mimeType) {
    Optional<String> firstMatch =
        sizerMap.keySet().stream().filter(regex -> mimeType.matches(regex)).findFirst();
    String lookupKey = firstMatch.orElse(UNKNOWN_MIME_TYPE);
    return Collections.unmodifiableList(sizerMap.get(lookupKey));
  }
}
