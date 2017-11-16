package com.github.ahoffer.sizeimage.test;

import com.github.jaiimageio.jpeg2000.impl.J2KImageReaderSpi;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.imageio.spi.IIORegistry;
import org.apache.commons.io.IOUtils;

public class TestData {

  public static final String JPEG = "JPEG";

  public static final String JPEG_2000 = "jpeg 2000";
  static final String INPUT_DIR = "/Users/aaronhoffer/data/small-image-set/";
  InputStream vanillaJpegStream;
  InputStream jpeg2000Stream;
  URL vanillaJpegUrl;
  URL jpeg2000Url;
  List<File> inputFiles = new ArrayList<>();

  public TestData() throws IOException {
    vanillaJpegUrl = getClass().getResource("/sample-jpeg.jpg");
    jpeg2000Url = getClass().getResource("/sample-jpeg2000.jpg");
    vanillaJpegStream = copy(getClass().getResourceAsStream("/sample-jpeg.jpg"));
    jpeg2000Stream = copy(getClass().getResourceAsStream("/sample-jpeg2000.jpg"));
    IIORegistry.getDefaultInstance().registerServiceProvider(J2KImageReaderSpi.class);

    inputFiles =
        Files.list(Paths.get(INPUT_DIR))
            .map(Path::toFile)
            .filter(File::isFile)
            .filter(file -> !file.getName().equals(".DS_Store"))
            .collect(Collectors.toList());
  }

  static InputStream copy(InputStream inputStream) throws IOException {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream(1024 * 1024);
    IOUtils.copy(inputStream, outputStream);
    return new ByteArrayInputStream(outputStream.toByteArray());
  }
}
