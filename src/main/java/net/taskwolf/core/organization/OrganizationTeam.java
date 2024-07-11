package net.taskwolf.core.organization;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.taskwolf.core.database.DatabaseRow;

import java.util.List;
import java.util.UUID;

@Accessors(fluent = true)
@AllArgsConstructor(staticName = "create")
public final class OrganizationTeam {
  public static OrganizationTeam of(DatabaseRow row) {
    return create(row.findCell(0).uuidValue(), row.findCell(1).uuidValue(),
      row.findCell(2).stringValue(), row.findCell(3).listValue());
  }

  @Getter
  private final UUID id;
  @Getter
  private final UUID organizationId;
  @Getter
  private String name;
  private final List<UUID> members;

  public void addMember(UUID member) {
    members.add(member);
  }

  public void removeMember(UUID member) {
    members.remove(member);
  }

  public void rename(String name) {
    this.name = name;
  }

  public List<UUID> members() {
    return List.copyOf(members);
  }
}
