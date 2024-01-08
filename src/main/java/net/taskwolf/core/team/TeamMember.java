package net.taskwolf.core.team;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.taskwolf.core.database.DatabaseRow;

import java.util.UUID;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(staticName = "create")
public final class TeamMember {
  public static TeamMember of(DatabaseRow row) {
    return create(row.findCell(0).uuidValue(),
      row.findCell(1).stringValue());
  }

  private final UUID userId;
  private final String group;
}
