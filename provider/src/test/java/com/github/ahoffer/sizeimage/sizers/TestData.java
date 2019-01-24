package com.github.ahoffer.sizeimage.sizers;

import com.github.jaiimageio.jpeg2000.impl.J2KImageReaderSpi;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import javax.imageio.spi.IIORegistry;

/** Test helper class. Encapsulates the I/O operations to provide some well-known test data. */
public class TestData {

  public static final int PIXELS = 100;
  static final String INPUT_DIR = "/Users/aaronhoffer/data/small-image-set/";
  InputStream vanillaJpeg_128x80_Stream;
  InputStream jpeg2000_128x80_Stream;
  InputStream jpeg2000_513x341_Stream;
  InputStream vanillaJpeg_300x200_Stream;
  List<File> inputFiles;

  public TestData() throws IOException {
    vanillaJpeg_128x80_Stream = getClass().getResourceAsStream("/sample-jpeg.jpg");
    jpeg2000_128x80_Stream = getClass().getResourceAsStream("/sample-jpeg2000.jp2");
    jpeg2000_513x341_Stream = getClass().getResourceAsStream("/airplane-jpeg2000.jp2");
    vanillaJpeg_300x200_Stream = getClass().getResourceAsStream("/crowd-17kb.jpg");
    IIORegistry.getDefaultInstance().registerServiceProvider(J2KImageReaderSpi.class);

    inputFiles =
        Files.list(Paths.get(INPUT_DIR))
            .map(Path::toFile)
            .filter(File::isFile)
            .filter(file -> !file.getName().equals(".DS_Store"))
            .collect(Collectors.toList());
  }
}
