package com.github.ahoffer.imageresize.provider;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.lang3.SystemUtils;
import org.im4java.core.ConvertCmd;
import org.im4java.core.IM4JavaException;
import org.im4java.core.IMOperation;
import org.im4java.core.Stream2BufferedImage;

import com.github.ahoffer.imageresize.api.AbstractImageResizer;
import com.github.ahoffer.imageresize.api.ImageResizer;

public class MagickResizer extends AbstractImageResizer {

    public static final String PATH_TO_IMAGE_MAGICK_EXECUTABLES = "pathToImageMagickExecutables";

    public static final String EXEC_NAME = "executableName";

    public static final String DEFAULT_OUTPUT_FORMAT = "png";

    public static final String WINDOWS_EXEC_NAME = "convert.exe";

    public static final String NIX_EXEC_NAME = "convert";

    public static final String INPUT_IMAGE_PATH = "inputImagePath";

    public static final String OUTPUT_FORMAT = "outputFormat";

    public boolean recommendedFor(String imageFormat) {
        return true;
    }

    @Override
    public boolean isAvailable() {
        return getImageMagickExecutable().canExecute();
    }

    File getImageMagickExecutable() {
        String execName = configuration.getOrDefault(EXEC_NAME,
                SystemUtils.IS_OS_WINDOWS ? WINDOWS_EXEC_NAME : NIX_EXEC_NAME);
        File exec;
        if (configuration.containsKey(PATH_TO_IMAGE_MAGICK_EXECUTABLES)) {
            exec = new File(configuration.get(PATH_TO_IMAGE_MAGICK_EXECUTABLES), execName);
        } else {
            exec = new File(execName);
        }
        return exec;
    }

    @Override
    public ImageResizer setInput(InputStream inputStream) {
        throw new java.lang.UnsupportedOperationException(
                getClass().getSimpleName() + " does not implement setInput()");
    }

    public BufferedImage resize() throws IOException {

        if (!isAvailable()) {
            throw new RuntimeException("Cannot resize image. ImageMagick executable not found.");
        }

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
        imageMagickOutputFormat = configuration.getOrDefault(OUTPUT_FORMAT, DEFAULT_OUTPUT_FORMAT);

        //TODO There is probably a better method than addImage() convert.setSearchPath() ?
        String outputFormatDirectedToStandardOut = imageMagickOutputFormat + ":-";
        op.addImage(outputFormatDirectedToStandardOut);

        if (configuration.containsKey(PATH_TO_IMAGE_MAGICK_EXECUTABLES)) {
            convert.setSearchPath(configuration.get(PATH_TO_IMAGE_MAGICK_EXECUTABLES));
        }

        try {
            convert.run(op);
        } catch (InterruptedException | IM4JavaException e) {
            throw new RuntimeException(
                    "Problem resizing image with ImageMagick." + " Check executable path."
                            + "Process does not inherit a PATH environment variable", e);
        }
        return outputConsumer.getImage();
    }
}

