/*
 * $RCSfile: FileFormatReader.java,v $
 * $Revision: 1.2 $
 * $Date: 2005/04/28 01:25:38 $
 * $State: Exp $
 *
 * Class:                   FileFormatReader
 *
 * Description:             Read J2K file stream
 *
 * COPYRIGHT:
 *
 * This software module was originally developed by Raphaël Grosbois and
 * Diego Santa Cruz (Swiss Federal Institute of Technology-EPFL); Joel
 * Askelöf (Ericsson Radio Systems AB); and Bertrand Berthelot, David
 * Bouchard, Félix Henry, Gerard Mozelle and Patrice Onno (Canon Research
 * Centre France S.A) in the course of development of the JPEG2000
 * standard as specified by ISO/IEC 15444 (JPEG 2000 Standard). This
 * software module is an implementation of a part of the JPEG 2000
 * Standard. Swiss Federal Institute of Technology-EPFL, Ericsson Radio
 * Systems AB and Canon Research Centre France S.A (collectively JJ2000
 * Partners) agree not to assert against ISO/IEC and users of the JPEG
 * 2000 Standard (Users) any of their rights under the copyright, not
 * including other intellectual property rights, for this software module
 * with respect to the usage by ISO/IEC and Users of this software module
 * or modifications thereof for use in hardware or software products
 * claiming conformance to the JPEG 2000 Standard. Those intending to use
 * this software module in hardware or software products are advised that
 * their use may infringe existing patents. The original developers of
 * this software module, JJ2000 Partners and ISO/IEC assume no liability
 * for use of this software module or modifications thereof. No license
 * or right to this software module is granted for non JPEG 2000 Standard
 * conforming products. JJ2000 Partners have full right to use this
 * software module for his/her own purpose, assign or donate this
 * software module to any third party and to inhibit third parties from
 * using this software module for non JPEG 2000 Standard conforming
 * products. This copyright notice must be included in all copies or
 * derivative works of this software module.
 *
 * Copyright (c) 1999/2000 JJ2000 Partners.
 * */

package com.github.ahoffer.sizeimage.support;

import com.github.jaiimageio.jpeg2000.impl.Box;
import com.github.jaiimageio.jpeg2000.impl.J2KImageReadParamJava;
import java.awt.color.ICC_Profile;
import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import jj2000.j2k.codestream.HeaderInfo;
import jj2000.j2k.codestream.Markers;
import jj2000.j2k.codestream.reader.HeaderDecoder;
import jj2000.j2k.fileformat.FileFormatBoxes;
import jj2000.j2k.io.RandomAccessIO;
import jj2000.j2k.util.ISRandomAccessIO;

/**
 * Hack-up of the FileFormatReader class for Jpeg20000. All the fields in class are private and
 * cannot inherited by subclasses. Many methods were touching those variables, but the changes are
 * not visible to a subclass. Easier just copy/paste the code and make necessary changes. The JAI
 * Jpeg2000 library is not under active development, so copy/paste should not cause maintenance
 * problems.
 *
 * <p>TODO: This class could use a little refactoring. It still has methods to read metadata that we
 * are not interested in. This logic can be removed and replace with calls to skip() to skip over
 * data we are not interested in.
 */
public class Jpeg2000MetadataMicroReader implements FileFormatBoxes {

  public static final List<Integer> JPX_FILE_FORMAT_BOXES =
      Arrays.asList(
          0x72726571,
          0x636f6d70,
          0x6a706368,
          0x6a706c68,
          0x61736f63,
          0x6e6c7374,
          0x66726565,
          0x726F6964,
          0x6c626c20,
          0x636F7074,
          0x696E7374,
          0x63677270,
          0x6f706374,
          0x63726567,
          0x6474626C,
          0x6674626C,
          0x666C7374,
          0x63726566,
          0x6D646174,
          0x6266696C,
          0x64726570,
          0x6774736F,
          0x6368636B,
          0x6D703762);
  public static final int READ_LIMIT = 8192;

  final InputStream inputStream;

  /** The random access from which the file format boxes are read */
  RandomAccessIO in;

  /** ICC profile */
  ICC_Profile profile;

  int heightFromImgHeaderBox;
  int widthFromImgHeaderBox;
  int widthFromCodeStreamStreamBox;
  int heightFromCodeStreamStreamBox;
  int minNumResolutionLevels;
  boolean isJpeg2000File;
  boolean codestreamBoxDetected;
  boolean sucessfullyRead;

  /**
   * The constructor of the FileFormatReader
   *
   * @param inputStream The input stream from which to read the file format
   */
  public Jpeg2000MetadataMicroReader(InputStream inputStream) throws IOException {
    // 1K should probably be enough. Look at lower the buffer size.
    // Also, ISRandomAcessIO will throw a throwable if consumers try to read past the cache limit.
    InputStream saferStream =
        inputStream.markSupported() ? inputStream : new BufferedInputStream(inputStream);
    this.inputStream = saferStream;
    this.in = new ISRandomAccessIO(inputStream, READ_LIMIT, READ_LIMIT, READ_LIMIT);
  }

  /**
   * Attempt to read file or codestream header of a JPEG 20000 stream Return true if successful.
   *
   * @exception java.io.IOException stream reset failed
   */
  public void read() throws IOException {

    inputStream.mark(READ_LIMIT);
    try {
      read0();
    } catch (IOException e) {
      sucessfullyRead = false;
    } finally {
      inputStream.reset();
    }

    sucessfullyRead = true;
  }

  void read0() throws IOException {

    int initialPos = in.getPos();

    // Make sure that the first 12 bytes is the JP2_SIGNATURE_BOX
    // or if not that the first 2 bytes is the SOC marker
    isJpeg2000File =
        in.readInt() == 0x0000000c
            && in.readInt() == JP2_SIGNATURE_BOX
            && in.readInt() == 0x0d0a870a;
    if (isJpeg2000File()) {
      // This method also tried to read fast file header and access codestream box
      whileLoop();
    } else {
      in.seek(initialPos);
      short twoBytes = in.readShort();
      codestreamBoxDetected = twoBytes == Markers.SOC;
      if (isCodestreamBoxDetected()) {
        // Stream represents a JPEG 2000 codestream. Read its header
        in.seek(initialPos);
        readContiguousCodeStreamBox();
      } else {
        throw new IOException("Could not find JPEG 2000 file metadata or a codestream");
      }
    }
  }

  /**
   * This method reads the File Type box
   *
   * @return false if the File Type box was not found or invalid else true
   * @exception java.io.IOException If an I/O error ocurred.
   * @exception java.io.EOFException If the end of file was reached
   */
  boolean readFileTypeBox(int length) throws IOException {
    int nComp;
    boolean foundComp = false;

    // Check for XLBox
    if (length == 1) { // Box has 8 byte length;
      long longLength = in.readLong();
      throw new IOException("File too long.");
    }

    // Check that this is a correct DBox value
    // Read Brand field
    if (in.readInt() != FT_BR) return false;

    // Read MinV field
    int minorVersion = in.readInt();

    // Check that there is at least one FT_BR entry in in
    // compatibility list
    nComp = (length - 16) / 4; // Number of compatibilities.
    int[] comp = new int[nComp];
    for (int i = 0; i < nComp; i++) {
      if ((comp[i] = in.readInt()) == FT_BR) foundComp = true;
    }
    if (!foundComp) return false;

    //    if (metadata != null) metadata.addNode(new FileTypeBox(FT_BR, minorVersion, comp));

    return true;
  }

  /**
   * This method reads the Image Header box
   *
   * @param length The length of the JP2Header box
   * @return false if the JP2Header box was not found or invalid else true
   * @exception java.io.IOException If an I/O error ocurred.
   * @exception java.io.EOFException If the end of file was reached
   */
  boolean readImageHeaderBox(int length) throws IOException, EOFException {

    if (length == 0) // This can not be last box
    throw new Error("Zero-length of JP2Header Box");

    heightFromImgHeaderBox = in.readInt();
    widthFromImgHeaderBox = in.readInt();

    //    numComp = in.readShort();
    //    bitDepth = in.readByte();
    //    compressionType = in.readByte();
    //    unknownColor = in.readByte();
    //    intelProp = in.readByte();

    return true;
  }

  /**
   * This method skips the Contiguous codestream box and adds position of contiguous codestream to a
   * vector
   *
   * @return false if the Contiguous codestream box was not found or invalid else true
   * @exception java.io.IOException If an I/O error ocurred.
   * @exception java.io.EOFException If the end of file was reached
   */
  boolean readContiguousCodeStreamBox() throws IOException {

    HeaderInfo hi = new HeaderInfo();
    HeaderDecoder hd;
    try {
      hd = new HeaderDecoder(in, new J2KImageReadParamJava(), hi);
    } catch (EOFException e) {
      throw new RuntimeException("J2KReadState2");
    } catch (IOException ioe) {
      throw new RuntimeException(ioe);
    }

    widthFromCodeStreamStreamBox = hd.getImgWidth();
    heightFromCodeStreamStreamBox = hd.getImgHeight();
    // Get minimum number of resolution levels available across all tile-components.
    // The docs indicate this is field is actually "decomposition levels", where decomposition
    // levels + 1 = resolution levels. But I'm not so sure. I need to get something that can read
    // JPEG 2000 metadata better than this JAI library to know.
    minNumResolutionLevels = hd.getDecoderSpecs().dls.getMin();

    return true;
  }

  /** This method reads the content of the palette box */
  void readPaletteBox() throws IOException {

    // TODO: Replace with skip statement.

    int lutSize = in.readShort();
    int numComp = in.readByte();
    byte[] compSize = new byte[numComp];

    for (int i = 0; i < numComp; i++) {
      compSize[i] = in.readByte();
    }

    byte[][] lut;
    lut = new byte[numComp][lutSize];

    for (int n = 0; n < lutSize; n++) {
      for (int c = 0; c < numComp; c++) {
        int depth = 1 + (compSize[c] & 0x7F);
        if (depth > 32) depth = 32;
        int numBytes = (depth + 7) >> 3;
        byte[] buf = new byte[numBytes];
        in.readFully(buf, 0, numBytes);

        int val = 0;

        for (int k = 0; k < numBytes; k++) {
          val = buf[k] + (val << 8);
        }
        lut[c][n] = (byte) val;
      }
    }
  }

  /** Read the component mapping channel. */
  void readComponentMappingBox(int length) throws IOException {

    // TODO: Replace with skip statement.
    int num = length / 4;

    short[] comps = new short[num];
    byte[] type = new byte[num];
    byte[] maps = new byte[num];

    for (int i = 0; i < num; i++) {
      comps[i] = in.readShort();
      type[i] = in.readByte();
      maps[i] = in.readByte();
    }
  }

  /**
   * This method reads the Channel Definition box
   *
   * @exception java.io.IOException If an I/O error ocurred.
   */
  void readChannelDefinitionBox() throws IOException {
    // TODO: Just read the short at the start of the box and then skip (short value) * (2 bytes)
    int num = in.readShort();
    short[] channels = new short[num];
    short[] cType = new short[num];
    short[] associations = new short[num];

    for (int i = 0; i < num; i++) {
      channels[i] = in.readShort();
      cType[i] = in.readShort();
      associations[i] = in.readShort();
    }
  }

  int skipBytes(int n) throws IOException {
    int bytes = in.skipBytes(n);
    if (bytes != n) {
      throw new RuntimeException("WTF?");
    }
    return bytes;
  }

  public int getWidth() {
    return widthFromImgHeaderBox == 0 ? widthFromCodeStreamStreamBox : widthFromImgHeaderBox;
  }

  public int getHeight() {
    return heightFromImgHeaderBox == 0 ? heightFromCodeStreamStreamBox : heightFromImgHeaderBox;
  }

  void whileLoop() throws IOException {
    int pos;
    int boxId;
    int boxLength;
    boolean jp2HeaderBoxFound = false;
    while (true) {
      boxLength = in.readInt();
      boxId = in.readInt();
      if (boxLength == 0) {
        // Zero length box is same as EOF
        return;
      } else if (boxLength == 1) {
        // If length of box is one, the length is too big to fit into an int. That's my
        // interpretation.
        in.readLong();
        throw new IOException("File too long.");
      }
      // Set position at 8 bytes (2 ints) into the "box"
      pos = in.getPos();
      // Reading box length and box type consumes two ints, which effectively shortens the legnth
      // by 8 bytes
      // Is it just me or does this seems like a back-assward way to do it? Why not just move the
      // stream pointer back 8 bytes? Cache misses or something?
      boxLength -= 8;

      switch (boxId) {
        case FILE_TYPE_BOX:
          // +8? Why? The stream is not reset to the start of the box, which means it could read 8
          // bytes past the so-called "end" of the box?
          readFileTypeBox(boxLength + 8);
          break;
        case CONTIGUOUS_CODESTREAM_BOX:
          if (!jp2HeaderBoxFound) {
            throw new Error(
                "Invalid JP2 file: JP2Header box not found before Contiguous codestream box");
          }
          // For sanity's sake, assume  only 1 code streams and stop looking.
          readContiguousCodeStreamBox();
          return;
        case JP2_HEADER_BOX:
          jp2HeaderBoxFound = true;
          break;
        case IMAGE_HEADER_BOX:
          readImageHeaderBox(boxLength);
          // We only want the image header box, so quit after it is read.
          // SCRATCH THAT. Try and get the code stream header to get the number of resolutions
          // layers
          break;
        case INTELLECTUAL_PROPERTY_BOX:
          skipBytes(boxLength);
          break;
        case XML_BOX:
          skipBytes(boxLength);
          break;
        case UUID_INFO_BOX:
          break;
        case UUID_BOX:
          skipBytes(boxLength);
          break;
        case UUID_LIST_BOX:
          skipBytes(boxLength);
          break;
        case URL_BOX:
          skipBytes(boxLength);
          break;
        case PALETTE_BOX:
          readPaletteBox();
          break;
        case BITS_PER_COMPONENT_BOX:
          skipBytes(boxLength);
          break;
        case COMPONENT_MAPPING_BOX:
          readComponentMappingBox(boxLength);
          break;
        case COLOUR_SPECIFICATION_BOX:
          skipBytes(boxLength);
          break;
        case CHANNEL_DEFINITION_BOX:
          readChannelDefinitionBox();
          break;
        case RESOLUTION_BOX:
          boxLength = 0;
          break;
        case CAPTURE_RESOLUTION_BOX:
        case DEFAULT_DISPLAY_RESOLUTION_BOX:
          skipBytes(boxLength);
          break;
        default:
          if (JPX_FILE_FORMAT_BOXES.contains(boxId)) {
            byte[] data = new byte[boxLength];
            in.readFully(data, 0, boxLength);
            // Unknown box type
            Box box = new Box(boxLength + 8, boxId, 0, data);
          } else {
            throw new RuntimeException("Unknown JPEG box ID");
          }
      }

      // Advance the stream to the next box
      in.seek(pos + boxLength);
    }
  }

  public int getMinNumResolutionLevels() {

    return minNumResolutionLevels;
  }

  public boolean isJpeg2000File() {
    return isJpeg2000File;
  }

  public boolean isCodestreamBoxDetected() {
    return codestreamBoxDetected;
  }

  public boolean isSucessfullyRead() {
    return sucessfullyRead;
  }
}
