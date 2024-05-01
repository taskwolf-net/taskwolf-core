package net.taskwolf.core.action;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.taskwolf.core.database.DatabaseRow;

import java.util.UUID;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(staticName = "create")
public final class ActionEntry {
  public static ActionEntry of(DatabaseRow row) {
    return create(row.findCell(0).uuidValue(), row.findCell(1).uuidValue(),
      row.findCell(2).uuidValue(), row.findCell(3).integerValue(),
      row.findCell(4).stringValue(), row.findCell(5).stringValue());
  }

  private final UUID id;
  private final UUID ownerId;
  private final UUID workflowId;
  private final int actionIndex;
  private final String module;
  private final String type;
}
