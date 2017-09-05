package com.github.ahoffer.imageresize.provider;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.im4java.core.ConvertCmd;
import org.im4java.core.IM4JavaException;
import org.im4java.core.IMOperation;
import org.im4java.core.Stream2BufferedImage;

import com.github.ahoffer.imageresize.api.AbstractImageResizer;
import com.github.ahoffer.imageresize.api.ImageResizer;

public class MagickResizer extends AbstractImageResizer {

    public static final String PATH_TO_IMAGE_MAGICK_EXECUTABLES = "pathToImageMagickExecutables";

    public static final String DEFAULT_OUTPUT_FORMAT = "png";

    public boolean recommendedFor(String imageFormat) {
        return true;
    }

    public static final String INPUT_IMAGE_PATH = "inputImagePath";

    public static final String OUTPUT_FORMAT = "outputFormat";

    public BufferedImage resize() throws IOException {

        if (!configuration.containsKey(INPUT_IMAGE_PATH)) {
            throw new IllegalArgumentException(
                    getClass().getSimpleName() + " expects a path to the input image");
        }

        String filePath = configuration.get(INPUT_IMAGE_PATH);

        File inputFile = new File(filePath);

        if (!inputFile.exists()) {
            throw new IllegalArgumentException(
                    getClass().getSimpleName() + " cannot find the input image file " + filePath);
        }

        if (!inputFile.canRead()) {
            throw new IllegalArgumentException(
                    getClass().getSimpleName() + " cannot find the input image file " + filePath);
        }

        IMOperation op = new IMOperation();
        ConvertCmd convert = new ConvertCmd();
        Stream2BufferedImage outputConsumer = new Stream2BufferedImage();
        convert.setOutputConsumer(outputConsumer);
        op.addImage(inputFile.getCanonicalPath());
        op.thumbnail(getOutputSize());
        String imageMagickOutputFormat;
        if (configuration.containsKey(OUTPUT_FORMAT)) {
            imageMagickOutputFormat = configuration.get(OUTPUT_FORMAT);
        } else {
            imageMagickOutputFormat = DEFAULT_OUTPUT_FORMAT; //PNG is about as good as anyone is going to do.
        }

        //TODO There is probably a better method than addImage() convert.setSearchPath() ?
        String outputFormatDirectedToStandardOut = imageMagickOutputFormat + ":-";
        op.addImage(outputFormatDirectedToStandardOut);

        if (configuration.containsKey(PATH_TO_IMAGE_MAGICK_EXECUTABLES)) {
            convert.setSearchPath(configuration.get(PATH_TO_IMAGE_MAGICK_EXECUTABLES));
        }

        try {
            convert.run(op);
        } catch (InterruptedException | IM4JavaException e) {
            throw new RuntimeException("Problem resizing and image with ImageMagick."
                    + "Check the path to the executuable."
                    + "Process does not inherit a PATH environment variable.", e);
        }
        return outputConsumer.getImage();
    }

    public ImageResizer setInput(InputStream inputStream) {
        return null;
    }
}

