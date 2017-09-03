package com.github.ahoffer.imageresize.provider;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.imageio.spi.IIORegistry;

import com.github.ahoffer.imageresize.api.ImageResizer;
import com.github.jaiimageio.jpeg2000.impl.J2KImageReaderSpi;

public class PreAllocatingResizer extends AbstractImageResizer {

    static {
        IIORegistry.getDefaultInstance()
                .registerServiceProvider(new J2KImageReaderSpi());
    }

    public ImageResizer setConfiguration(Map<String, String> configuration) {
        return null;
    }

    public BufferedImage resize() {

        BufferedImage output = new BufferedImage(source.getWidth(null),
                source.getHeight(null),
                BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = output.createGraphics();
        graphics.drawImage(source, null, null);
        graphics.dispose();
        lastThumbnail = Scalr.resize(output, thumbSize);    }

    public boolean recommened(String imageFormat) {
        return false;
    }
}
