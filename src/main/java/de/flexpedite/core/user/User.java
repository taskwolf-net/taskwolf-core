package de.flexpedite.core.user;

import de.flexpedite.core.database.DatabaseRow;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.UUID;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(staticName = "create")
public final class User {
  public static User of(DatabaseRow row) {
    return create(row.findCell(0).uuidValue(), row.findCell(1).stringValue(),
      row.findCell(2).stringValue(), row.findCell(3).stringValue(),
      row.findCell(4).listValue());
  }

  private final UUID id;
  private final String name;
  private final String email;
  private final String passwordHash;
  private final List<UUID> organizations;
}
