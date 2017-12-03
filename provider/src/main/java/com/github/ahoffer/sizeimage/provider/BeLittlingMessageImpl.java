package com.github.ahoffer.sizeimage.provider;

import com.github.ahoffer.sizeimage.BeLittlingMessage;
import java.util.Optional;

public class BeLittlingMessageImpl implements BeLittlingMessage {

  String id;
  String description;
  BeLittlingSeverity severity;
  Exception exception;

  @SuppressWarnings("unsed")
  private BeLittlingMessageImpl() {}

  public BeLittlingMessageImpl(String id, BeLittlingSeverity severity, String description) {
    this.id = id;
    this.severity = severity;
    this.description = description;
  }

  public BeLittlingMessageImpl(
      String id, BeLittlingSeverity severity, String description, Exception exception) {
    this(id, severity, description);
    this.exception = exception;
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public String getDescription() {

    return description;
  }

  @Override
  public BeLittlingSeverity getSeverity() {

    return severity;
  }

  public String toString() {
    return ""
        + (getId() == null ? "??" : getId())
        + ", "
        + (getSeverity() == null ? "??" : getSeverity().toString())
        + ", "
        + (getDescription() == null ? "??" : getDescription());
  }

  @Override
  public Optional<Exception> getException() {
    return Optional.ofNullable(exception);
  }
}
