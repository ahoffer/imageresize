package com.github.ahoffer.sizeimage;

import java.util.Optional;

public interface BeLittlingMessage {
  public String getId();

  public String getDescription();

  public BeLittlingSeverity getSeverity();

  public Optional<Exception> getException();

  public enum BeLittlingSeverity {
    ERROR,
    WARNING,
    INFO
  }
}
