package net.taskwolf.core.ticket;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.taskwolf.core.database.DatabaseRow;

import java.util.List;
import java.util.UUID;

@Accessors(fluent = true)
@RequiredArgsConstructor(staticName = "create")
public final class Ticket {
  public static Ticket of(DatabaseRow row) {
    return create(row.findCell(0).uuidValue(), row.findCell(1).uuidValue(),
      row.findCell(2).stringValue(), Type.valueOf(row.findCell(3).stringValue()),
      Status.valueOf(row.findCell(4).stringValue()), row.findCell(5).listValue());
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
  private final String title;
  @Getter
  private final Type type;
  @Getter
  private final Status status;
  private final List<UUID> messages;

  public void addMessage(UUID message) {
    messages.add(message);
  }

  public void removeMessage(UUID message) {
    messages.remove(message);
  }

  public List<UUID> messages() {
    return List.copyOf(messages);
  }
}
