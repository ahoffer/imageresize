package belittle;

import belittle.BeLittleMessage.BeLittleSeverity;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class ResizableImageImpl implements ResizableImage {

  ImageInputFile file;
  BeLittleSizerSetting setting;
  List<BeLittleResult> results;
  ImageSizerCollection sizers;

  public ResizableImageImpl(
      ImageInputFile file, BeLittleSizerSetting setting, ImageSizerCollection sizers) {
    this.file = file;
    this.setting = new BeLittleSizerSettingImpl(setting);
    this.sizers = sizers;
  }

  @Override
  public BufferedImage resize(int width, int height, boolean preserveAspectRatio) {
    // preserveAspectRatio not implemented
    results = new ArrayList<>();
    setting.setWidth(width);
    setting.setHeight(height);
    return resize();
  }

  BufferedImage resize() {
    List<ImageSizer> sizerList = sizers.getSizersForMimeType(file.getMimeType());
    for (ImageSizer sizer : sizerList) {
      BeLittleResult result = sizer.getResult();
      results.add(result);
      //      Callable<BeLittleResult> callable = () -> sizer.resize(source.openBufferedStream());
      //      Future<BeLittleResult> future = executorService.submit(callable);
      try {
        sizer.resize(file);
        //        future.get(sizerSetting.getTimeoutSeconds(), TimeUnit.SECONDS);
      } catch (
          /*InterruptedException | ExecutionException | TimeoutException |*/ RuntimeException e) {
        sizer.addMessage(new BeLittleMessageImpl("EXP", BeLittleSeverity.ERROR, e));
      }
      if (result.succeeded()) {
        // Return immediately if a sizer succeeds.
        return result.getOutput();
      }
    }

    // If no sizer succeeds, return results from all sizers.
    // return sizerList.stream().map(ImageSizer::getResult).collect(Collectors.toList());
    return null;
  }

  @Override
  public List<BeLittleResult> getResults() {
    return results;
  }
}
