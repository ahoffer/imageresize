package com.github.ahoffer.sizeimage.provider;

import com.github.jaiimageio.jpeg2000.impl.J2KImageReaderSpi;
import java.io.InputStream;
import java.net.URL;
import javax.imageio.spi.IIORegistry;

public class SizeImageTestData {

  public static final String JPEG = "JPEG";

  public static final String JPEG_2000 = "jpeg 2000";

  InputStream vanillaJpegStream;

  InputStream jpeg2000Stream;

  URL vanillaJpegUrl;

  URL jpeg2000Url;

  public SizeImageTestData() {
    vanillaJpegUrl = getClass().getResource("/sample-jpeg.jpg");
    jpeg2000Url = getClass().getResource("/sample-jpeg2000.jpg");
    vanillaJpegStream = getClass().getResourceAsStream("/sample-jpeg.jpg");
    jpeg2000Stream = getClass().getResourceAsStream("/sample-jpeg2000.jpg");
    IIORegistry.getDefaultInstance().registerServiceProvider(J2KImageReaderSpi.class);
  }
}
