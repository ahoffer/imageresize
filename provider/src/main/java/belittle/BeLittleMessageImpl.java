package belittle;

import java.util.Optional;

public class BeLittleMessageImpl implements BeLittleMessage {

  String id;
  String description;
  BeLittleSeverity severity;
  Throwable throwable;

  public BeLittleMessageImpl(String id, BeLittleSeverity severity, String description) {
    this.id = id;
    this.severity = severity;
    this.description = description;
  }

  public BeLittleMessageImpl(
      String id, BeLittleSeverity severity, String description, Throwable throwable) {
    this(id, severity, description);
    this.throwable = throwable;
  }

  public BeLittleMessageImpl(String id, BeLittleSeverity severity, Throwable throwable) {
    this(id, severity, throwable.getMessage(), throwable);
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
  public BeLittleSeverity getSeverity() {

    return severity;
  }

  public String toString() {
    return String.format(
        "%s, %s, %s",
        getSeverity() == null ? "??" : getSeverity().toString(),
        getId() == null ? "??" : getId(),
        getDescription() == null ? "??" : getDescription());
  }

  @Override
  public Optional<Throwable> getThrowable() {
    return Optional.ofNullable(throwable);
  }
}
