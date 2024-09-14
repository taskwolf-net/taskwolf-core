package com.dulno.core.notification;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import com.dulno.core.database.DatabaseRow;

import java.util.UUID;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(staticName = "create")
public final class NotificationSetting {
  public static NotificationSetting of(DatabaseRow row) {
    return create(row.findCell(0).uuidValue(), row.findCell(1).booleanValue(),
      row.findCell(2).booleanValue());
  }

  private final UUID userId;
  private final boolean general;
  private final boolean workflowFail;
}
