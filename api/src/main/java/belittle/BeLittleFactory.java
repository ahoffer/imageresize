package belittle;

public interface BeLittleFactory {

  BeLittle newBeLittle(BeLittleSizerSetting settings);

  BeLittle newBeLittle();

  BeLittleSizerSetting newSetting();
}
