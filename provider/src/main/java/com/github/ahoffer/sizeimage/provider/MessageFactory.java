package com.github.ahoffer.sizeimage.provider;

import static com.github.ahoffer.sizeimage.provider.MessageConstants.BAD_HEIGHT;
import static com.github.ahoffer.sizeimage.provider.MessageConstants.BAD_WIDTH;
import static com.github.ahoffer.sizeimage.provider.MessageConstants.CANNOT_READ_WIDTH_AND_HEIGHT;
import static com.github.ahoffer.sizeimage.provider.MessageConstants.DECODE_JPEG2000;
import static com.github.ahoffer.sizeimage.provider.MessageConstants.EXTERNAL_EXECUTABLE;
import static com.github.ahoffer.sizeimage.provider.MessageConstants.MISSING_INPUT_STREAM;
import static com.github.ahoffer.sizeimage.provider.MessageConstants.NO_SIZER;
import static com.github.ahoffer.sizeimage.provider.MessageConstants.OPJ_FAILED;
import static com.github.ahoffer.sizeimage.provider.MessageConstants.OS_PROCESS_FAILED;
import static com.github.ahoffer.sizeimage.provider.MessageConstants.OS_PROCESS_INTERRUPTED;
import static com.github.ahoffer.sizeimage.provider.MessageConstants.RESIZE_ERROR;
import static com.github.ahoffer.sizeimage.provider.MessageConstants.RESOLUTION_LEVELS;
import static com.github.ahoffer.sizeimage.provider.MessageConstants.SAMPLE_PERIOD;
import static com.github.ahoffer.sizeimage.provider.MessageConstants.SIZER_NAME;
import static com.github.ahoffer.sizeimage.provider.MessageConstants.UNABLE_TO_CREATE_TEMP_FILE;
import static com.github.ahoffer.sizeimage.provider.MessageConstants.UNKNOWN_MESSAGE_ID;

import com.github.ahoffer.sizeimage.BeLittlingMessage;
import com.github.ahoffer.sizeimage.BeLittlingMessage.BeLittlingSeverity;

public class MessageFactory {

  public BeLittlingMessage make(String id, Object... values) {
    //    boolean hasOneValue = values.length ==1;
    switch (id) {
      case BAD_WIDTH:
        return new BeLittlingMessageImpl(
            BAD_WIDTH,
            BeLittlingSeverity.ERROR,
            String.format("%d is not a valid size", values[0]));
      case BAD_HEIGHT:
        return new BeLittlingMessageImpl(
            BAD_HEIGHT,
            BeLittlingSeverity.ERROR,
            String.format("%d is not a valid size", values[0]));
      case SIZER_NAME:
        return new BeLittlingMessageImpl(SIZER_NAME, BeLittlingSeverity.INFO, (String) values[0]);
      case EXTERNAL_EXECUTABLE:
        return new BeLittlingMessageImpl(
            EXTERNAL_EXECUTABLE, BeLittlingSeverity.ERROR, (String) values[0]);
      case MISSING_INPUT_STREAM:
        return new BeLittlingMessageImpl(
            MISSING_INPUT_STREAM, BeLittlingSeverity.ERROR, "Input stream cannot be null");
      case RESIZE_ERROR:
        return new BeLittlingMessageImpl(
            RESIZE_ERROR,
            BeLittlingSeverity.ERROR,
            "Exception caught generating the output",
            (Exception) values[0]);
      case NO_SIZER:
        return new BeLittlingMessageImpl(
            NO_SIZER, BeLittlingSeverity.ERROR, "No image sizer could be found");
      case DECODE_JPEG2000:
        return new BeLittlingMessageImpl(
            DECODE_JPEG2000,
            BeLittlingSeverity.ERROR,
            "Exception attempting to read/decode JPEG 2000 image. Is it a different kind of image?");
      case SAMPLE_PERIOD:
        return new BeLittlingMessageImpl(
            SAMPLE_PERIOD, BeLittlingSeverity.INFO, String.format("Sampling period=%d", values[0]));
      case RESOLUTION_LEVELS:
        return new BeLittlingMessageImpl(
            RESOLUTION_LEVELS,
            BeLittlingSeverity.INFO,
            String.format("Resolutions levels decoded %d", values[0]));
      case UNABLE_TO_CREATE_TEMP_FILE:
        return new BeLittlingMessageImpl(
            UNABLE_TO_CREATE_TEMP_FILE,
            BeLittlingSeverity.ERROR,
            "Could not create temporary file for input image");
      case OS_PROCESS_FAILED:
        return new BeLittlingMessageImpl(
            OS_PROCESS_FAILED, BeLittlingSeverity.ERROR, ((Exception) values[0]).getMessage());
      case OPJ_FAILED:
        return new BeLittlingMessageImpl(OPJ_FAILED, BeLittlingSeverity.ERROR, (String) values[0]);
      case CANNOT_READ_WIDTH_AND_HEIGHT:
        return new BeLittlingMessageImpl(
            CANNOT_READ_WIDTH_AND_HEIGHT,
            BeLittlingSeverity.WARNING,
            "Could not read width and height of image. Using resolution level 0 (maximum)");
      case OS_PROCESS_INTERRUPTED:
        return new BeLittlingMessageImpl(
            OS_PROCESS_INTERRUPTED, BeLittlingSeverity.ERROR, "OS process interrupted");
      default:
        return makeUnrecognized(id);
    }
  }

  BeLittlingMessageImpl makeUnrecognized(String unknownMessageId) {
    return new BeLittlingMessageImpl(
        UNKNOWN_MESSAGE_ID,
        BeLittlingSeverity.ERROR,
        String.format("%s is unrecognized", unknownMessageId));
  }
}
