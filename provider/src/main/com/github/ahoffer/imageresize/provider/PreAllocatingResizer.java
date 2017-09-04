package com.github.ahoffer.imageresize.provider;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.imageio.spi.IIORegistry;

import com.github.ahoffer.imageresize.api.AbstractInMemoryImageResizer;
import com.github.jaiimageio.jpeg2000.impl.J2KImageReaderSpi;

public class PreAllocatingResizer extends AbstractInMemoryImageResizer {

    static {
        IIORegistry.getDefaultInstance()
                .registerServiceProvider(new J2KImageReaderSpi());
    }

    public BufferedImage resize() throws IOException {
        Image inputImage = ImageIO.read(inputStream);
        if (null != inputImage) {
            BufferedImage imageCopy = new BufferedImage(inputImage.getWidth(null),
                    inputImage.getHeight(null),
                    BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics = imageCopy.createGraphics();
            graphics.drawImage(inputImage, null, null);
            graphics.dispose();
            return super.resize(imageCopy);
        }
        return null;
    }

    public boolean recommendedFor(String imageFormat) {
        return JPEG_2000_FORMAT_NAME.equalsIgnoreCase(imageFormat);
    }
}
