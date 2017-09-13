package com.github.ahoffer.imageresize.bundle;

import java.io.InputStream;
import java.net.URL;

import javax.imageio.spi.IIORegistry;

import com.github.jaiimageio.jpeg2000.impl.J2KImageReaderSpi;

public class ImageResizeTestData {

    public static final String JPEG = "JPEG";

    public static final String JPEG_2000 = "jpeg 2000";

    InputStream vanillaJpegStream;

    InputStream jpeg2000Stream;

    URL vanillaJpegUrl;

    URL jpeg2000Url;

    public ImageResizeTestData() {
        vanillaJpegUrl = getClass().getResource("/sample-jpeg.jpg");
        jpeg2000Url = getClass().getResource("/sample-jpeg2000.jpg");
        vanillaJpegStream = getClass().getResourceAsStream("/sample-jpeg.jpg");
        jpeg2000Stream = getClass().getResourceAsStream("/sample-jpeg2000.jpg");
        IIORegistry.getDefaultInstance()
                .registerServiceProvider(J2KImageReaderSpi.class);
    }
}
