package com.github.ahoffer.sizeimage.test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class TestData {

  public static final int PIXELS = 100;
  static final String INPUT_DIR = "/Users/aaronhoffer/data/small-image-set/";
  InputStream vanillaJpeg_128x80Stream;
  InputStream jpeg2000_128x80Stream;
  InputStream jpeg2000_513x341Stream;
  InputStream jpeg_300x200Stream;
  List<File> inputFiles;

  public TestData() throws IOException {
    vanillaJpeg_128x80Stream = getClass().getResourceAsStream("/sample-jpeg.jpg");
    jpeg2000_128x80Stream = getClass().getResourceAsStream("/sample-jpeg2000.jp2");
    jpeg2000_513x341Stream = getClass().getResourceAsStream("/airplane-jpeg2000.jp2");
    jpeg_300x200Stream = getClass().getResourceAsStream("/airplane-jpeg2000.jp2");

    inputFiles =
        Files.list(Paths.get(INPUT_DIR))
            .map(Path::toFile)
            .filter(File::isFile)
            .filter(file -> !file.getName().equals(".DS_Store"))
            .collect(Collectors.toList());
  }
}
