package net.taskwolf.core.user;

import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.Getter;
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
      row.findCell(4).stringValue(), row.findCell(5).listValue(),
      row.findCell(6).booleanValue(), row.findCell(7).booleanValue(),
      row.findCell(8).longValue());
  }

  public static User unknown(UUID id) {
    return create(id, "Unknown", "Unknown", "", "", Lists.newArrayList(),
      true, true, -1);
  }

  private final UUID id;
  private String name;
  private String email;
  private String passwordHash;
  private String language;
  private final List<UUID> organizations;
  private final boolean legalAccepted;
  private final boolean newsletter;
  private final long joinDate;

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
