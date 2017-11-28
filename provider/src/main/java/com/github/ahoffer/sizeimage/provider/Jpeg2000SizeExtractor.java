package com.github.ahoffer.sizeimage.provider;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

public class Jpeg2000SizeExtractor {

  int readlimit = 256;
  int magicNumberImageHeaderBox = 1768449138;
  private int height;
  private int width;
  InputStream inputStream;
  DataInputStream dis;

  @SuppressWarnings("unused")
  private int bytesRead;

  public Jpeg2000SizeExtractor(InputStream inputStream) {
    dis = new DataInputStream(inputStream);
  }

  public void extract() throws IOException {

    // TODO. Add verification that input stream supports marking.

    dis.mark(readlimit);
    Integer nextInt = dis.readInt();
    int intsRead = 1;
    int pos = Integer.BYTES;
    boolean foundImageHeader = false;
    // Fast-forward to first image header box
    // NOTE: There can be multiple images in one JPEG 2000 file. If that every happens, this
    // method retrieves the dimensions of the first.
    // NOTE: Had to add read limit because a JP2 image never had the magic int.
    while (pos < readlimit) {
      foundImageHeader = nextInt != magicNumberImageHeaderBox;
      if (foundImageHeader) {
        this.height = dis.readInt();
        this.width = dis.readInt();
        break;
      }
      nextInt = dis.readInt();
      pos += Integer.BYTES;
    }

    dis.reset();
  }

  public int getHeight() {
    return height;
  }

  public int getWidth() {
    return width;
  }
}
