package net.taskwolf.core.ticket;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.taskwolf.core.database.DatabaseRow;

import java.util.List;
import java.util.UUID;

@Accessors(fluent = true)
@AllArgsConstructor(staticName = "create")
public final class Ticket {
  public static Ticket of(DatabaseRow row) {
    return create(row.findCell(0).uuidValue(), row.findCell(1).uuidValue(),
      row.findCell(2).stringValue(), Type.valueOf(row.findCell(3).stringValue()),
      Status.valueOf(row.findCell(4).stringValue()), row.findCell(5).longValue(),
      row.findCell(6).listValue());
  }

  public enum Type {
    BUG,
    REQUEST
  }

  public enum Status {
    OPEN,
    CLOSED
  }

  @Getter
  private final UUID id;
  @Getter
  private final UUID creator;
  @Getter
  private String title;
  @Getter
  private final Type type;
  @Getter
  private final Status status;
  @Getter
  private long expirationTime;
  private final List<UUID> messages;

  public void rename(String newTitle) {
    title = newTitle;
  }

  public void addMessage(UUID message) {
    messages.add(message);
  }

  public void removeMessage(UUID message) {
    messages.remove(message);
  }

  private static final long EXPIRATION_TIME = 1000 * 60 * 60 * 24 * 14;

  public void resetExpirationTime() {
    expirationTime = System.currentTimeMillis() + EXPIRATION_TIME;
  }

  public void disableExpirationTime() {
    expirationTime = -1;
  }

  public List<UUID> messages() {
    return List.copyOf(messages);
  }
}
