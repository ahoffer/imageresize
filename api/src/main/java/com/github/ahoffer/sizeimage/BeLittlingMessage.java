package com.github.ahoffer.sizeimage;

public interface BeLittlingMessage {
  public String getId();

  public String getDescription();

  public BeLittlingSeverity getSeverity();

  public enum BeLittlingSeverity {
    ERROR,
    WARNING,
    INFO
  }
}
