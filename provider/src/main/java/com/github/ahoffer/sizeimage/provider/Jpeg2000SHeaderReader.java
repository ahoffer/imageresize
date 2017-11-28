package com.github.ahoffer.sizeimage.provider;

import com.github.jaiimageio.jpeg2000.impl.J2KImageReadParamJava;
import java.io.IOException;
import java.io.InputStream;
import jj2000.j2k.codestream.HeaderInfo;
import jj2000.j2k.codestream.reader.HeaderDecoder;
import jj2000.j2k.util.ISRandomAccessIO;

/** This code was copied fromo the class com.github.jaiimageio.jpeg2000.impl.J2KReadState */
public class Jpeg2000SHeaderReader {

  private int height;
  private int width;
  private InputStream inputStream;
  private int resolutionLevels;

  public Jpeg2000SHeaderReader(InputStream inputStream) {
    this.inputStream = inputStream;
  }

  public void readHeader() throws IOException {
    HeaderInfo hi = new HeaderInfo();
    ISRandomAccessIO in = new ISRandomAccessIO(inputStream, 8096, 8096, 8096);
    HeaderDecoder hd = new HeaderDecoder(in, new J2KImageReadParamJava(), hi);
    this.width = hd.getImgWidth();
    this.height = hd.getImgHeight();
    // Get minimum number of resolution levels available across
    // all tile-components.
    this.resolutionLevels = hd.getDecoderSpecs().dls.getMin();
  }

  public int getHeight() {
    return height;
  }

  public int getWidth() {
    return width;
  }

  public int getResolutionLevels() {
    return resolutionLevels;
  }
}
