package com.github.ahoffer.sizeimage.provider;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

public class PreAllocatingServiceSize extends AbstractInMemorySizeImageService {

    public BufferedImage size() throws IOException {
        validateBeforeSize();
        Image inputImage = ImageIO.read(inputStream);
        if (null != inputImage) {
            BufferedImage imageCopy = new BufferedImage(inputImage.getWidth(null),
                    inputImage.getHeight(null),
                    BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics = imageCopy.createGraphics();
            graphics.drawImage(inputImage, null, null);
            graphics.dispose();
            return this.size(imageCopy);
        }
        return null;
    }

    public boolean recommendedFor(String imageFormat) {
        return JPEG_2000_FORMAT_NAME.equalsIgnoreCase(imageFormat);
    }
}
