package net.taskwolf.core.workflow.operation;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.taskwolf.core.database.DatabaseRow;

import java.util.UUID;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(staticName = "create")
public final class Operation {
  public static Operation of(DatabaseRow row) {
    return create(row.findCell(0).uuidValue(), row.findCell(1).longValue(),
      row.findCell(2).longValue());
  }

  private final UUID targetId;
  private final long operations;
  private final long expiration;
}
