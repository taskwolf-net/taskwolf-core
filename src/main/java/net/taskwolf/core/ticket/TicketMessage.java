package net.taskwolf.core.ticket;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.taskwolf.core.database.DatabaseRow;

import java.util.UUID;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(staticName = "create")
public class TicketMessage {
  public static TicketMessage of(DatabaseRow row) {
    return create(row.findCell(0).uuidValue(), row.findCell(1).uuidValue(),
      row.findCell(2).uuidValue(), row.findCell(3).stringValue(),
      row.findCell(4).longValue());
  }

  private final UUID id;
  private final UUID ticket;
  private final UUID author;
  private final String message;
  private final long time;
}
