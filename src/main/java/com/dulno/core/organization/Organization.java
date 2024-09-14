package com.dulno.core.organization;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;
import com.dulno.core.database.DatabaseRow;

import java.util.List;
import java.util.UUID;

@Accessors(fluent = true)
@AllArgsConstructor(staticName = "create")
public final class Organization {
  public static Organization of(DatabaseRow row) {
    return create(row.findCell(0).uuidValue(), row.findCell(1).stringValue(),
      row.findCell(2).uuidValue(), row.findCell(3).listValue(),
      row.findCell(4).stringValue());
  }

  @Getter
  private final UUID id;
  @Getter
  private String name;
  @Getter
  private final UUID owner;
  private final List<UUID> members;
  @Getter
  private String invitationToken;

  public void addMember(UUID member) {
    members.add(member);
  }

  public void removeMember(UUID member) {
    members.remove(member);
  }

  public void rename(String name) {
    this.name = name;
  }

  public void changeInvitationToken(String token) {
    invitationToken = token;
  }

  public List<UUID> members() {
    return List.copyOf(members);
  }
}
