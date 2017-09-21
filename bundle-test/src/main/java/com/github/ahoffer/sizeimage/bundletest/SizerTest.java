package com.github.ahoffer.sizeimage.bundletest;

import com.github.ahoffer.sizeimage.ImageSizer;
import com.github.ahoffer.sizeimage.bundle.ImageSizerFactory;
import static com.github.ahoffer.sizeimage.provider.ImageMagickSizer.OUTPUT_FORMAT;
import static com.github.ahoffer.sizeimage.provider.ImageMagickSizer.PATH_TO_IMAGE_MAGICK_EXECUTABLES;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;

public class SizerTest {

    String inputDir = "/Users/aaronhoffer/data/small-image-set/";

    String outputDir = inputDir + "output/";

    private ImageSizerFactory imageSizerFactory;

    @SuppressWarnings("unused")
    public void init() throws Exception {
//        ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
//        exec.schedule(this::runTest, 1, TimeUnit.SECONDS);
//        exec.shutdown();
        runTest();

    }


    void runTest() {

        Map<String, String> configuration = new HashMap<>();
        configuration.put(PATH_TO_IMAGE_MAGICK_EXECUTABLES, "/opt/local/bin/");
        configuration.put(OUTPUT_FORMAT, "png");
        try {
            final File[] files = (new File(inputDir)).listFiles();
            for (int i = 0; i < files.length; ++i) {
                final File file = files[i];
                if (!file.isDirectory() && !file.getName()
                        .equals(".DS_Store")) {
                    List<ImageSizer> sizers = getImageSizerFactory().getImageSizers();
                    for (ImageSizer sizer : sizers) {
                        InputStream inputStream =
                                new BufferedInputStream(new FileInputStream(file));
                        final long start = System.nanoTime();
                        System.out.println(String.format("\nStarting file %s of size %.2f MB...",
                                file.getName(),
                                file.length() / 1e6));
                        sizer.setConfiguration(configuration);
                        String sizerName = sizer.getClass()
                                .getSimpleName();
                        System.out.println(String.format("Selected %s", sizerName));

                        BufferedImage output = sizer.setOutputSize(256)
                                .setInput(inputStream)
                                .size();
                        final long stop = System.nanoTime();
                        java.io.File outputDirObject = new File(outputDir);
                        outputDirObject.mkdirs();
                        java.io.File outputFile = new File(outputDirObject,
                                sizerName + "-" + file.getName() + ".png");
                        ImageIO.write(output, "png", outputFile);
                        System.out.println(String.format("...Created %s thumbnail in %.2f s",
                                file.getName(),
                                (stop - start) / 1.0e9));
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Exception. Oh snap!");
            System.err.println(e.getMessage());
            e.printStackTrace();
            //throw new RuntimeException(e);
        }
    }

    public ImageSizerFactory getImageSizerFactory() {
        return imageSizerFactory;
    }

    @SuppressWarnings("unused")
    public void setImageSizerFactory(ImageSizerFactory imageSizerFactory) {
        this.imageSizerFactory = imageSizerFactory;
    }
}
