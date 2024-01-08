package net.taskwolf.core.team;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.taskwolf.core.database.DatabaseRow;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(staticName = "create")
public final class Group {
  public static Group of(DatabaseRow row) {
    return create(row.findCell(0).stringValue(),
      row.findCell(1).integerValue());
  }

  private final String name;
  private final int permission;
}
