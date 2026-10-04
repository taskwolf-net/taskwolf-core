package net.taskwolf.core.ticket;

import net.taskwolf.core.database.DatabaseColumn;
import net.taskwolf.core.database.DatabaseRow;
import net.taskwolf.core.database.DatabaseTable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.UUID;

@Accessors(fluent = true)
@AllArgsConstructor(staticName = "create")
public final class Ticket {
  public static Ticket of(DatabaseRow row, DatabaseTable table) {
    return of(row, table.columns().stream().map(DatabaseColumn::name).toList());
  }

  public static Ticket of(DatabaseRow row, List<String> columns) {
    return create(row.findCell(columns.indexOf("creator")).uuidValue(),
      row.findCell(columns.indexOf("id")).uuidValue(),
      row.findCell(columns.indexOf("title")).stringValue(),
      Type.valueOf(row.findCell(columns.indexOf("type")).stringValue()),
      Status.valueOf(row.findCell(columns.indexOf("status")).stringValue()),
      row.findCell(columns.indexOf("expirationTime")).longValue(),
      row.findCell(columns.indexOf("messages")).listValue(),
      row.findCell(columns.indexOf("lastMessageSeen")).booleanValue());
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
  private final UUID creator;
  @Getter
  private final UUID id;
  @Getter
  private String title;
  @Getter
  private final Type type;
  @Getter
  private Status status;
  @Getter
  private long expirationTime;
  private final List<UUID> messages;
  @Getter
  private boolean lastMessageSeen;

  public void rename(String newTitle) {
    title = newTitle;
  }

  public void updateStatus(Status newStatus) {
    status = newStatus;
  }

  public void addMessage(UUID message) {
    messages.add(message);
  }

  public void removeMessage(UUID message) {
    messages.remove(message);
  }

  private static final long EXPIRATION_TIME = 1000L * 60 * 60 * 24 * 14;

  public void resetExpirationTime() {
    expirationTime = System.currentTimeMillis() + EXPIRATION_TIME;
  }

  public void disableExpirationTime() {
    expirationTime = -1;
  }

  public List<UUID> messages() {
    return List.copyOf(messages);
  }

  public void updateLastMessageSeen(boolean newLastMessageSeen) {
    lastMessageSeen = newLastMessageSeen;
  }
}
