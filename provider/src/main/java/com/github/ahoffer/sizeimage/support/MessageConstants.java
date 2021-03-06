package com.github.ahoffer.sizeimage.support;

// TODO: Should this be in the API module? Answers: No. The BeLittlingMessage interface in the API
// is good enough. These contstants have been changing willy-nilly throughout development and should
// not be considered an API.
// NOTE: Did not make this an enumeration because it is not possible to extending enumerations
// with subclassing and I wanted other modules/
public interface MessageConstants {
  String BAD_WIDTH = "BAD_WIDTH";
  String BAD_HEIGHT = "BAD_HEIGHT";
  String MISSING_INPUT_STREAM = "MISSING_INPUT_STREAM";
  String UNKNOWN_MESSAGE_ID = "UNKNOWN_MESSAGE_ID";
  String RESIZE_ERROR = "RESIZE_ERROR";
  String NO_SIZER = "NO_SIZER";
  String SIZER_NAME = "SIZER_NAME";
  String DECODE_JPEG2000 = "DECODE_JPEG2000";
  String EXTERNAL_EXECUTABLE = "EXTERNAL_EXECUTABLE";
  String SAMPLE_PERIOD = "SAMPLE_PERIOD";
  String RESOLUTION_LEVELS = "RESOLUTION_LEVELS";
  String UNABLE_TO_CREATE_TEMP_FILE = "UNABLE_TO_CREATE_TEMP_FILE";
  String OPJ_FAILED = "OPJ_FAILED";
  String CANNOT_READ_WIDTH_AND_HEIGHT = "CANNOT_READ_WIDTH_AND_HEIGHT";
  String OS_PROCESS_INTERRUPTED = "OS_PROCESS_INTERRUPTED";
  String OS_PROCESS_FAILED = "OS_PROCESS_FAILED";
  String STREAM_MANGLED = "STREAM_MANGLED";
  String BIT_RATE = "BIT_RATE";
  String COULD_NOT_READ_METADATA = "COULD_NOT_READ_METADATA";
  String THREAD_INTERRUPTED = "THREAD_INTERRUPTED";
  String EXECUTION_EXCEPTION = "EXECUTION_EXCEPTION";
  String TIMEOUT = "TIMEOUT";
  String UNCONFIGURED = "UNCONFIGURED";
  String COULD_NOT_READ_IMAGE = "COULD_NOT_READ_IMAGE";
  String COULD_NOT_CLOSE_STREAM = "COULD_NOT_CLOSE_STREAM";
  String REDUCTION_FACTOR = "REDUCTION_FACTOR";
  String NO_IMAGE_READER = "NO_IMAGE_READER";
}
