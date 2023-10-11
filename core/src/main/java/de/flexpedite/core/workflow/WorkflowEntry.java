package de.flexpedite.core.workflow;

import de.flexpedite.core.database.DatabaseRow;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.UUID;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(staticName = "create")
public final class WorkflowEntry {
  public static WorkflowEntry of(DatabaseRow row) {
    return create(row.findCell(0).uuidValue(), row.findCell(1).uuidValue(),
      row.findCell(2).uuidValue(), row.findCell(3).listValue());
  }

  private final UUID id;
  private final UUID userId;
  private final UUID triggerId;
  private final List<UUID> actionIds;
}
