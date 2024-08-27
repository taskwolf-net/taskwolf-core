package net.taskwolf.core.session;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.taskwolf.core.database.DatabaseColumn;
import net.taskwolf.core.database.DatabaseRow;
import net.taskwolf.core.database.DatabaseTable;

import java.util.List;
import java.util.UUID;

@Getter
@Accessors(fluent = true)
@AllArgsConstructor(staticName = "create")
public final class Session {
  public static Session of(DatabaseRow row, DatabaseTable table) {
    return of(row, table.columns().stream().map(DatabaseColumn::name).toList());
  }

  public static Session of(DatabaseRow row, List<String> columns) {
    return create(row.findCell(columns.indexOf("id")).uuidValue(),
      row.findCell(columns.indexOf("user")).uuidValue(),
      SessionStatus.valueOf(row.findCell(columns.indexOf("status")).stringValue()),
      row.findCell(columns.indexOf("devicePlatform")).stringValue(),
      row.findCell(columns.indexOf("ipAddress")).stringValue(),
      row.findCell(columns.indexOf("country")).stringValue(),
      row.findCell(columns.indexOf("city")).stringValue(),
      row.findCell(columns.indexOf("openTime")).longValue(),
      row.findCell(columns.indexOf("refreshToken")).stringValue());
  }

  private final UUID id;
  private final UUID userId;
  private SessionStatus status;
  private final String devicePlatform;
  private final String ipAddress;
  private final String country;
  private final String city;
  private final long openTime;
  private String lastRefreshToken;

  public void close() {
    status = SessionStatus.CLOSED;
  }

  public void updateRefreshToken(String refreshToken) {
    lastRefreshToken = refreshToken;
  }
}
