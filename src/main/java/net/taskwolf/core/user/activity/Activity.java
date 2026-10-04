package net.taskwolf.core.user.activity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.taskwolf.core.database.DatabaseRow;

import java.util.UUID;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(staticName = "create")
public final class Activity {
  public static Activity of(DatabaseRow row) {
    return create(row.findCell(0).uuidValue(), row.findCell(1).uuidValue(),
      row.findCell(2).stringValue(), row.findCell(3).stringValue(),
      row.findCell(4).longValue(),
      ActivityType.valueOf(row.findCell(5).stringValue()));
  }

  private final UUID id;
  private final UUID userId;
  private final String title;
  private final String description;
  private final long time;
  private final ActivityType type;
}
