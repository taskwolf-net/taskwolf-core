package net.taskwolf.core.organization;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.taskwolf.core.database.DatabaseRow;

import java.util.List;
import java.util.UUID;

@Accessors(fluent = true)
@RequiredArgsConstructor(staticName = "create")
public final class Organization {
  public static Organization of(DatabaseRow row) {
    return create(row.findCell(0).uuidValue(), row.findCell(1).stringValue(),
      row.findCell(2).uuidValue(), row.findCell(3).listValue());
  }

  @Getter
  private final UUID id;
  @Getter
  private final String name;
  @Getter
  private final UUID owner;
  private final List<UUID> members;

  public void addMember(UUID member) {
    members.add(member);
  }

  public void removeMember(UUID member) {
    members.remove(member);
  }

  public List<UUID> members() {
    return List.copyOf(members);
  }
}
