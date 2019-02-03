package belittle.support;

import static belittle.BeLittleMessage.BeLittlingSeverity.ERROR;
import static belittle.BeLittleMessage.BeLittlingSeverity.INFO;
import static belittle.BeLittleMessage.BeLittlingSeverity.WARNING;
import static belittle.support.MessageConstants.BAD_HEIGHT;
import static belittle.support.MessageConstants.BAD_WIDTH;
import static belittle.support.MessageConstants.BIT_RATE;
import static belittle.support.MessageConstants.CANNOT_READ_WIDTH_AND_HEIGHT;
import static belittle.support.MessageConstants.COULD_NOT_CLOSE_STREAM;
import static belittle.support.MessageConstants.COULD_NOT_READ_IMAGE;
import static belittle.support.MessageConstants.COULD_NOT_READ_METADATA;
import static belittle.support.MessageConstants.DECODE_JPEG2000;
import static belittle.support.MessageConstants.EXTERNAL_EXECUTABLE;
import static belittle.support.MessageConstants.MISSING_INPUT_STREAM;
import static belittle.support.MessageConstants.NO_IMAGE_READER;
import static belittle.support.MessageConstants.NO_SIZER;
import static belittle.support.MessageConstants.OPJ_FAILED;
import static belittle.support.MessageConstants.OS_PROCESS_FAILED;
import static belittle.support.MessageConstants.OS_PROCESS_INTERRUPTED;
import static belittle.support.MessageConstants.REDUCTION_FACTOR;
import static belittle.support.MessageConstants.RESIZE_ERROR;
import static belittle.support.MessageConstants.RESOLUTION_LEVELS;
import static belittle.support.MessageConstants.SAMPLE_PERIOD;
import static belittle.support.MessageConstants.SIZER_NAME;
import static belittle.support.MessageConstants.STREAM_MANGLED;
import static belittle.support.MessageConstants.UNABLE_TO_CREATE_TEMP_FILE;
import static belittle.support.MessageConstants.UNCONFIGURED;
import static belittle.support.MessageConstants.UNKNOWN_MESSAGE_ID;

import belittle.BeLittleMessage;
import belittle.BeLittleMessageImpl;

/**
 * This class serves two purposes. First, it provides a shortcut for adding messages to ImageSizers.
 * Second, it is a way to ensure consistency of messages. The same ID string should always produce
 * the same (or consistently similar) messages.
 */
public class MessageFactory {

  public BeLittleMessage make(String id, Object... values) {
    // TODO: Make array access safe - extract values into Optional<Object> or Optional<?>. Or just
    // todo: catch the exception accessing a nonexistent array element and generate a message to
    // that
    // todo: effect.
    //    boolean hasOneValue = values.length ==1;
    switch (id) {
      case BAD_WIDTH:
        return new BeLittleMessageImpl(
            BAD_WIDTH, ERROR, String.format("%d is not a valid size", (int) values[0]));
      case BAD_HEIGHT:
        return new BeLittleMessageImpl(
            BAD_HEIGHT, ERROR, String.format("%d is not a valid size", (int) values[0]));
      case SIZER_NAME:
        return new BeLittleMessageImpl(SIZER_NAME, INFO, values[0].toString());
      case EXTERNAL_EXECUTABLE:
        return new BeLittleMessageImpl(
            EXTERNAL_EXECUTABLE,
            ERROR,
            "Executable not found. Check executable path. "
                + "Process does not inherit a PATH environment variable");
      case MISSING_INPUT_STREAM:
        return new BeLittleMessageImpl(MISSING_INPUT_STREAM, ERROR, "Input stream cannot be null");
      case RESIZE_ERROR:
        return new BeLittleMessageImpl(RESIZE_ERROR, ERROR, (Exception) values[0]);

      case NO_SIZER:
        return new BeLittleMessageImpl(NO_SIZER, ERROR, "No image sizer could be found");
      case DECODE_JPEG2000:
        return new BeLittleMessageImpl(
            DECODE_JPEG2000,
            ERROR,
            "Exception attempting to read/decode JPEG 2000 image. Is it a different kind of image?");
      case SAMPLE_PERIOD:
        return new BeLittleMessageImpl(
            SAMPLE_PERIOD, INFO, String.format("Sampling period=%d", (int) values[0]));
      case RESOLUTION_LEVELS:
        return new BeLittleMessageImpl(
            RESOLUTION_LEVELS,
            INFO,
            String.format("Resolutions levels decoded %d", (int) values[0]));
      case UNABLE_TO_CREATE_TEMP_FILE:
        return new BeLittleMessageImpl(
            UNABLE_TO_CREATE_TEMP_FILE, ERROR, "Could not create temporary file for input image");
      case OS_PROCESS_FAILED:
        return new BeLittleMessageImpl(
            OS_PROCESS_FAILED, ERROR, ((Exception) values[0]).getMessage());
      case OPJ_FAILED:
        return new BeLittleMessageImpl(OPJ_FAILED, ERROR, values[0].toString());
      case CANNOT_READ_WIDTH_AND_HEIGHT:
        return new BeLittleMessageImpl(
            CANNOT_READ_WIDTH_AND_HEIGHT,
            WARNING,
            "Could not read width and height of image. Using resolution level 0 (maximum)");
      case OS_PROCESS_INTERRUPTED:
        return new BeLittleMessageImpl(OS_PROCESS_INTERRUPTED, ERROR, "OS process interrupted");
      case STREAM_MANGLED:
        return new BeLittleMessageImpl(STREAM_MANGLED, ERROR, ((Exception) values[0]).getMessage());
      case BIT_RATE:
        return new BeLittleMessageImpl(BIT_RATE, INFO, values[0].toString());
      case COULD_NOT_READ_METADATA:
        return new BeLittleMessageImpl(
            COULD_NOT_READ_METADATA,
            ERROR,
            "Could not read metadata from image. Image might be corrupt");
      case UNCONFIGURED:
        return new BeLittleMessageImpl(UNCONFIGURED, WARNING, values[0].toString());
      case COULD_NOT_READ_IMAGE:
        return new BeLittleMessageImpl(COULD_NOT_READ_IMAGE, ERROR, (Exception) values[0]);
      case COULD_NOT_CLOSE_STREAM:
        return new BeLittleMessageImpl(COULD_NOT_CLOSE_STREAM, WARNING, (Exception) values[0]);
      case REDUCTION_FACTOR:
        return new BeLittleMessageImpl(REDUCTION_FACTOR, INFO, values[0].toString());
      case NO_IMAGE_READER:
        return new BeLittleMessageImpl(NO_IMAGE_READER, ERROR, "No compatible JAI reader found");

      default:
        return makeUnrecognized(id);
    }
  }

  BeLittleMessageImpl makeUnrecognized(String unknownMessageId) {
    return new BeLittleMessageImpl(
        UNKNOWN_MESSAGE_ID, ERROR, String.format("%s is unrecognized", unknownMessageId));
  }
}
