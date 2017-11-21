package com.github.ahoffer.sizeimage.provider;

import static com.github.ahoffer.sizeimage.provider.MessageConstants.BAD_WIDTH;
import static com.github.ahoffer.sizeimage.provider.MessageConstants.MISSING_INPUT_STREAM;
import static com.github.ahoffer.sizeimage.provider.MessageConstants.NO_SIZER;
import static com.github.ahoffer.sizeimage.provider.MessageConstants.RESIZE_ERROR;
import static com.github.ahoffer.sizeimage.provider.MessageConstants.UNKNOWN_MESSAGE_ID;

import com.github.ahoffer.sizeimage.BeLittlingMessage;
import com.github.ahoffer.sizeimage.BeLittlingMessage.BeLittlingSeverity;

public class MessageFactory {

  public BeLittlingMessage make(String id, Object actualValue) {
    switch (id) {
      case BAD_WIDTH:
        return new BeLittlingMessageImpl(
            BAD_WIDTH,
            BeLittlingSeverity.ERROR,
            String.format("%d is not a valid size", actualValue));
      default:
        return makeUnrecognized(id);
    }
  }

  public BeLittlingMessage make(String id) {
    switch (id) {
      case MISSING_INPUT_STREAM:
        return new BeLittlingMessageImpl(
            MISSING_INPUT_STREAM, BeLittlingSeverity.ERROR, "Input stream cannot be null");
      case RESIZE_ERROR:
        return new BeLittlingMessageImpl(
            RESIZE_ERROR, BeLittlingSeverity.ERROR, "Exception caught generating the output");
      case NO_SIZER:
        return new BeLittlingMessageImpl(
            NO_SIZER, BeLittlingSeverity.ERROR, "No image sizer could be found");
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
