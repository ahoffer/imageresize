package com.github.ahoffer.sizeimage;

import java.util.Optional;

/**
 * Working with images means working with input/output as well as data structures that may or may
 * not be entirely compatible with the libraries that read, write , and manipulate them. A lot can
 * go wrong and it is easy to produce unintended results or no results. To simplify using and
 * debugging this library, the ImageSizer collects messages throughout its operation and returns
 * them along with the results. A message includes of an ID, used to identify where it was generated
 * and, at a high level, about it is about. A message also includes its severity (error, warning, or
 * informational). A message can optionally include an exception (throwable) to aid debugging.
 */
public interface BeLittlingMessage {

  /**
   * The ID is a string that identifies the message at a high level. Implementers are free to
   * designate their own IDs strings and it is expected that IDs will be stored as static final
   * constants.
   *
   * @return the string that
   */
  String getId();

  /**
   * Free-text description of the message.
   *
   * @return description
   */
  String getDescription();

  /**
   * Return the severity of the message. See the definition of the enummeration for more
   * information.
   *
   * @return informational, warning, or error
   */
  BeLittlingSeverity getSeverity();

  /**
   * Often a message is created in reponse to an Exception. The exception can be attached to the
   * message for information about the conditional, including a stacktrace.
   *
   * @return
   */
  Optional<Throwable> getThrowable();

  /**
   * Messages have three designtions: errors, warnings, and information. Informational messages
   * provide facts aand context, such as what sizer was used, it's configuration, or start/finish
   * time. Warnings indicate potential problems, sucha as missing configuration values. Errors are
   * hard stops. They indicate an unrecoverable event.
   */
  enum BeLittlingSeverity {
    ERROR,
    WARNING,
    INFO
  }
}
