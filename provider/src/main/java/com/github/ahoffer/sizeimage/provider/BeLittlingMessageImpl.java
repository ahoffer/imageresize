package com.github.ahoffer.sizeimage.provider;

import com.github.ahoffer.sizeimage.BeLittlingMessage;

public class BeLittlingMessageImpl implements BeLittlingMessage {

  String id;
  String description;
  BeLittlingSeverity severity;

  @SuppressWarnings("unsed")
  private BeLittlingMessageImpl() {}

  public BeLittlingMessageImpl(String id, BeLittlingSeverity severity, String description) {
    this.id = id;
    this.severity = severity;
    this.description = description;
  }

  @Override
  public String getId() {
    return null;
  }

  @Override
  public String getDescription() {

    return null;
  }

  @Override
  public BeLittlingSeverity getSeverity() {

    return null;
  }
}
