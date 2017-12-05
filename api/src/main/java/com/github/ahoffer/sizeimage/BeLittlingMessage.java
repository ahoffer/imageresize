package com.github.ahoffer.sizeimage;

import java.util.Optional;

public interface BeLittlingMessage {
  String getId();

  String getDescription();

  BeLittlingSeverity getSeverity();

  Optional<Throwable> getThrowable();

  enum BeLittlingSeverity {
    ERROR,
    WARNING,
    INFO
  }
}
