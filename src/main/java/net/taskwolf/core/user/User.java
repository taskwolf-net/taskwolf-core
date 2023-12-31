package net.taskwolf.core.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.taskwolf.core.database.DatabaseRow;

import java.util.List;
import java.util.UUID;

@Getter
@Accessors(fluent = true)
@AllArgsConstructor(staticName = "create")
public final class User {
  public static User of(DatabaseRow row) {
    return create(row.findCell(0).uuidValue(), row.findCell(1).stringValue(),
      row.findCell(2).stringValue(), row.findCell(3).stringValue(),
      row.findCell(4).listValue());
  }

  private final UUID id;
  private final String name;
  private String email;
  private String passwordHash;
  private final List<UUID> organizations;

  public void addOrganization(UUID organization) {
    organizations.add(organization);
  }

  public void removeOrganization(UUID organization) {
    organizations.remove(organization);
  }

  public void changePassword(String newPasswordHash) {
    passwordHash = newPasswordHash;
  }

  public void changeEmail(String newEmail) {
    email = newEmail;
  }
}
