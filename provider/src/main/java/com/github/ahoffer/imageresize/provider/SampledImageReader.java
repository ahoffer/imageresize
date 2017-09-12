package com.github.ahoffer.imageresize.provider;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;

public class SampledImageReader {
    //TODO: Maybe change some of these fields to Optional value holders.
    protected int samplePeriod;

    int imageIndex;

    ImageReader reader;

    InputStream source;

    private int subsamplingHint = 512;

    static public SampledImageReader of(InputStream source) throws IOException {
        SampledImageReader object = new SampledImageReader();
        object.source = source;
        object.reader = GetImageReader.get(source);
        return object;
    }

    static public SampledImageReader of(File sourceFile) throws IOException {
        SampledImageReader object = new SampledImageReader();
        object.source = new FileInputStream(sourceFile);
        object.reader = GetImageReader.get(new FileInputStream(sourceFile));
        return object;
    }

    public SampledImageReader subsamplingHint(int hint) {
        subsamplingHint = hint;
        return this;
    }

    public SampledImageReader imageIndex(int index) {
        imageIndex = index;
        return this;
    }

    public SampledImageReader samplePeriod(int period) {
        samplePeriod = period;
        return this;
    }

    public int computeSamplingPeriod() {
        if (samplePeriod == 0) {
            try {
                int longestDimensionSize = Math.max(reader.getWidth(imageIndex),
                        reader.getHeight(imageIndex));
                samplePeriod = (int) (Math.round(Math.ceil(
                        longestDimensionSize / (double) subsamplingHint)));

            } catch (IOException e) {
                //Give up. Do not sub-sample the image.
                samplePeriod = 1;
            }
        }
        return samplePeriod;
    }

    public BufferedImage read() throws IOException {
        BufferedImage image;
        int columnOffset = 0;
        int rowOffset = 0;
        // Use the same sampling period for both rows and columns to preserve images's
        // aspect ratio.
        int columnSamplingPeriod = computeSamplingPeriod();
        int rowSamplingPeriod = computeSamplingPeriod();
        ImageReadParam imageParam = reader.getDefaultReadParam();
        try {
            imageParam.setSourceSubsampling(columnSamplingPeriod,
                    rowSamplingPeriod,
                    columnOffset,
                    rowOffset);
            image = reader.read(imageIndex, imageParam);
        } finally {
            source.close();
            reader.dispose();
        }

        return image;
    }
}
