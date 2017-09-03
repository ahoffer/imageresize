package com.github.ahoffer.imageresize.provider;

import javax.imageio.spi.IIORegistry;

import com.github.jaiimageio.jpeg2000.impl.J2KImageReaderSpi;

public class PreAllocatingResizer {

    static {
        IIORegistry.getDefaultInstance()
                .registerServiceProvider(new J2KImageReaderSpi());
    }
}
