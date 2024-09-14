package com.dulno.core.maintenance;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;
import com.dulno.core.database.DatabaseRow;

import java.util.UUID;

@Getter
@Accessors(fluent = true)
@AllArgsConstructor(staticName = "create")
public final class Maintenance {
  public static Maintenance of(DatabaseRow row) {
    return create(row.findCell(0).uuidValue(),
      row.findCell(1).stringValue(), row.findCell(2).longValue(),
      row.findCell(3).longValue(),
      MaintenanceStatus.valueOf(row.findCell(4).stringValue()));
  }

  private final UUID id;
  private final String description;
  private final long startTime;
  private final long duration;
  private MaintenanceStatus status;

  public void updateStatus(MaintenanceStatus newStatus) {
    status = newStatus;
  }
}