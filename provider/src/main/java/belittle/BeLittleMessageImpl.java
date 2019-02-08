package belittle;

import java.util.Optional;

public class BeLittleMessageImpl implements BeLittleMessage {

  String description;
  BeLittleSeverity severity;
  Throwable throwable;

  public BeLittleMessageImpl(BeLittleSeverity severity, String description) {
    this.severity = severity;
    this.description = description;
  }

  public BeLittleMessageImpl(BeLittleSeverity severity, String description, Throwable throwable) {
    this(severity, description);
    this.throwable = throwable;
  }

  public BeLittleMessageImpl(String id, BeLittleSeverity severity, Throwable throwable) {
    this(severity, throwable.getMessage(), throwable);
  }

  @Override
  public String getDescription() {

    return description;
  }

  @Override
  public BeLittleSeverity getSeverity() {

    return severity;
  }

  public String toString() {
    return String.format(
        "%s, %s",
        getSeverity() == null ? "??" : getSeverity().toString(),
        getDescription() == null ? "??" : getDescription());
  }

  @Override
  public Optional<Throwable> getThrowable() {
    return Optional.ofNullable(throwable);
  }
}
