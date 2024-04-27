package net.taskwolf.core.grafana;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.taskwolf.core.database.DatabaseRow;

import java.util.UUID;

@Getter
@Accessors(fluent = true)
@AllArgsConstructor(staticName = "create")
public final class GrafanaAccount {
  public static GrafanaAccount of(DatabaseRow row) {
    return create(row.findCell(0).uuidValue(), row.findCell(1).stringValue(),
      row.findCell(2).stringValue(), row.findCell(3).integerValue(),
      row.findCell(4).integerValue(), row.findCell(5).stringValue(),
      row.findCell(6).integerValue(), row.findCell(7).stringValue());
  }

  private final UUID ownerId;
  private final String username;
  private final String password;
  private final int userId;
  private final int organizationId;
  private final String datasourceUid;
  private int datasourceVersion;
  private final String dashboardUrl;

  public void updateDatasourceVersion(int datasourceVersion) {
    this.datasourceVersion = datasourceVersion;
  }
}
