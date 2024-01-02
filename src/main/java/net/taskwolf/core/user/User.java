package net.taskwolf.core.user;

import com.google.common.collect.Lists;
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
      row.findCell(4).stringValue(), row.findCell(5).listValue());
  }

  public static User unknown(UUID id) {
    return create(id, "Unknown", "Unknown", "", "", Lists.newArrayList());
  }

  private final UUID id;
  private String name;
  private String email;
  private String passwordHash;
  private String language;
  private final List<UUID> organizations;

  public void addOrganization(UUID organization) {
    organizations.add(organization);
  }

  public void removeOrganization(UUID organization) {
    organizations.remove(organization);
  }

  public void changeName(String newName) {
    name = newName;
  }

  public void changeEmail(String newEmail) {
    email = newEmail;
  }

  public void changePassword(String newPasswordHash) {
    passwordHash = newPasswordHash;
  }

  public void changeLanguage(String newLanguage) {
    language = newLanguage;
  }
}
