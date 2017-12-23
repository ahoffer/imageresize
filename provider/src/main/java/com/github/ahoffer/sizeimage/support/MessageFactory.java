package com.github.ahoffer.sizeimage.support;

import static com.github.ahoffer.sizeimage.BeLittlingMessage.BeLittlingSeverity.ERROR;
import static com.github.ahoffer.sizeimage.BeLittlingMessage.BeLittlingSeverity.INFO;
import static com.github.ahoffer.sizeimage.BeLittlingMessage.BeLittlingSeverity.WARNING;
import static com.github.ahoffer.sizeimage.support.MessageConstants.BAD_HEIGHT;
import static com.github.ahoffer.sizeimage.support.MessageConstants.BAD_WIDTH;
import static com.github.ahoffer.sizeimage.support.MessageConstants.BIT_RATE;
import static com.github.ahoffer.sizeimage.support.MessageConstants.CANNOT_READ_WIDTH_AND_HEIGHT;
import static com.github.ahoffer.sizeimage.support.MessageConstants.COULD_NOT_CLOSE_STREAM;
import static com.github.ahoffer.sizeimage.support.MessageConstants.COULD_NOT_READ_IMAGE;
import static com.github.ahoffer.sizeimage.support.MessageConstants.COULD_NOT_READ_METADATA;
import static com.github.ahoffer.sizeimage.support.MessageConstants.DECODE_JPEG2000;
import static com.github.ahoffer.sizeimage.support.MessageConstants.EXTERNAL_EXECUTABLE;
import static com.github.ahoffer.sizeimage.support.MessageConstants.MISSING_INPUT_STREAM;
import static com.github.ahoffer.sizeimage.support.MessageConstants.NO_IMAGE_READER;
import static com.github.ahoffer.sizeimage.support.MessageConstants.NO_SIZER;
import static com.github.ahoffer.sizeimage.support.MessageConstants.OPJ_FAILED;
import static com.github.ahoffer.sizeimage.support.MessageConstants.OS_PROCESS_FAILED;
import static com.github.ahoffer.sizeimage.support.MessageConstants.OS_PROCESS_INTERRUPTED;
import static com.github.ahoffer.sizeimage.support.MessageConstants.REDUCTION_FACTOR;
import static com.github.ahoffer.sizeimage.support.MessageConstants.RESIZE_ERROR;
import static com.github.ahoffer.sizeimage.support.MessageConstants.RESOLUTION_LEVELS;
import static com.github.ahoffer.sizeimage.support.MessageConstants.SAMPLE_PERIOD;
import static com.github.ahoffer.sizeimage.support.MessageConstants.SIZER_NAME;
import static com.github.ahoffer.sizeimage.support.MessageConstants.STREAM_MANGLED;
import static com.github.ahoffer.sizeimage.support.MessageConstants.UNABLE_TO_CREATE_TEMP_FILE;
import static com.github.ahoffer.sizeimage.support.MessageConstants.UNCONFIGURED;
import static com.github.ahoffer.sizeimage.support.MessageConstants.UNKNOWN_MESSAGE_ID;

import com.github.ahoffer.sizeimage.BeLittlingMessage;

public class MessageFactory {

  public BeLittlingMessage make(String id, Object... values) {
    // TODO: Make array access safe - extract values into Optional<Object> or Optional<?>. Or just
    // todo: catch the exception accessing a nonexistent array elemnt and generate a message to that
    // todo: effect.
    //    boolean hasOneValue = values.length ==1;
    switch (id) {
      case BAD_WIDTH:
        return new BeLittlingMessageImpl(
            BAD_WIDTH, ERROR, String.format("%d is not a valid size", (int) values[0]));
      case BAD_HEIGHT:
        return new BeLittlingMessageImpl(
            BAD_HEIGHT, ERROR, String.format("%d is not a valid size", (int) values[0]));
      case SIZER_NAME:
        return new BeLittlingMessageImpl(SIZER_NAME, INFO, values[0].toString());
      case EXTERNAL_EXECUTABLE:
        return new BeLittlingMessageImpl(
            EXTERNAL_EXECUTABLE,
            ERROR,
            "Executable not found. Check executable path. "
                + "Process does not inherit a PATH environment variable");
      case MISSING_INPUT_STREAM:
        return new BeLittlingMessageImpl(
            MISSING_INPUT_STREAM, ERROR, "Input stream cannot be null");
      case RESIZE_ERROR:
        return new BeLittlingMessageImpl(RESIZE_ERROR, ERROR, (Exception) values[0]);

      case NO_SIZER:
        return new BeLittlingMessageImpl(NO_SIZER, ERROR, "No image sizer could be found");
      case DECODE_JPEG2000:
        return new BeLittlingMessageImpl(
            DECODE_JPEG2000,
            ERROR,
            "Exception attempting to read/decode JPEG 2000 image. Is it a different kind of image?");
      case SAMPLE_PERIOD:
        return new BeLittlingMessageImpl(
            SAMPLE_PERIOD, INFO, String.format("Sampling period=%d", (int) values[0]));
      case RESOLUTION_LEVELS:
        return new BeLittlingMessageImpl(
            RESOLUTION_LEVELS,
            INFO,
            String.format("Resolutions levels decoded %d", (int) values[0]));
      case UNABLE_TO_CREATE_TEMP_FILE:
        return new BeLittlingMessageImpl(
            UNABLE_TO_CREATE_TEMP_FILE, ERROR, "Could not create temporary file for input image");
      case OS_PROCESS_FAILED:
        return new BeLittlingMessageImpl(
            OS_PROCESS_FAILED, ERROR, ((Exception) values[0]).getMessage());
      case OPJ_FAILED:
        return new BeLittlingMessageImpl(OPJ_FAILED, ERROR, values[0].toString());
      case CANNOT_READ_WIDTH_AND_HEIGHT:
        return new BeLittlingMessageImpl(
            CANNOT_READ_WIDTH_AND_HEIGHT,
            WARNING,
            "Could not read width and height of image. Using resolution level 0 (maximum)");
      case OS_PROCESS_INTERRUPTED:
        return new BeLittlingMessageImpl(OS_PROCESS_INTERRUPTED, ERROR, "OS process interrupted");
      case STREAM_MANGLED:
        return new BeLittlingMessageImpl(
            STREAM_MANGLED, ERROR, ((Exception) values[0]).getMessage());
      case BIT_RATE:
        return new BeLittlingMessageImpl(BIT_RATE, INFO, values[0].toString());
      case COULD_NOT_READ_METADATA:
        return new BeLittlingMessageImpl(
            COULD_NOT_READ_METADATA,
            ERROR,
            "Could not read metadata from image. Image might be corrupt");
      case UNCONFIGURED:
        return new BeLittlingMessageImpl(UNCONFIGURED, WARNING, values[0].toString());
      case COULD_NOT_READ_IMAGE:
        return new BeLittlingMessageImpl(COULD_NOT_READ_IMAGE, ERROR, (Exception) values[0]);
      case COULD_NOT_CLOSE_STREAM:
        return new BeLittlingMessageImpl(COULD_NOT_CLOSE_STREAM, WARNING, (Exception) values[0]);
      case REDUCTION_FACTOR:
        return new BeLittlingMessageImpl(REDUCTION_FACTOR, INFO, values[0].toString());
      case NO_IMAGE_READER:
        return new BeLittlingMessageImpl(NO_IMAGE_READER, ERROR, "No compatible JAI reader found");

      default:
        return makeUnrecognized(id);
    }
  }

  BeLittlingMessageImpl makeUnrecognized(String unknownMessageId) {
    return new BeLittlingMessageImpl(
        UNKNOWN_MESSAGE_ID, ERROR, String.format("%s is unrecognized", unknownMessageId));
  }
}
