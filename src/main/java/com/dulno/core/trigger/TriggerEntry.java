package com.dulno.core.trigger;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;
import com.dulno.core.database.DatabaseRow;

import java.util.UUID;

@Getter
@Accessors(fluent = true)
@AllArgsConstructor(staticName = "create")
public final class TriggerEntry {
  public static TriggerEntry of(DatabaseRow row) {
    return create(row.findCell(0).uuidValue(), row.findCell(1).uuidValue(),
      row.findCell(2).uuidValue(), row.findCell(3).stringValue(),
      row.findCell(4).stringValue(),
      TriggerState.valueOf(row.findCell(5).stringValue()));
  }

  private final UUID id;
  private final UUID ownerId;
  private final UUID workflowId;
  private final String module;
  private final String type;
  private TriggerState state;

  public void changeState(TriggerState state) {
    this.state = state;
  }
}
