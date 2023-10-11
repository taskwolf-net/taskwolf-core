package de.flexpedite.core.action;

import de.flexpedite.core.database.DatabaseRow;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import java.util.UUID;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(staticName = "create")
public final class ActionEntry {
  public static ActionEntry of(DatabaseRow row) {
    return create(row.findCell(0).uuidValue(), row.findCell(1).uuidValue(),
      row.findCell(2).uuidValue(), row.findCell(3).stringValue(),
      row.findCell(4).stringValue(), row.findCell(5).stringValue());
  }

  private final UUID id;
  private final UUID userId;
  private final UUID workflowId;
  private final String module;
  private final String type;
  private final String content;
}
