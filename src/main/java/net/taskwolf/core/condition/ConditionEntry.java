package net.taskwolf.core.condition;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.taskwolf.core.database.DatabaseRow;

import java.util.UUID;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(staticName = "create")
public final class ConditionEntry {
  public static ConditionEntry of(DatabaseRow row) {
    return create(row.findCell(0).uuidValue(), row.findCell(1).uuidValue(),
      row.findCell(2).uuidValue(), row.findCell(3).integerValue(),
      row.findCell(4).integerValue(), row.findCell(5).stringValue(),
      row.findCell(6).stringValue());
  }

  private final UUID id;
  private final UUID ownerId;
  private final UUID workflowId;
  private final int actionIndex;
  private final int conditionIndex;
  private final String type;
  private final String content;
}
