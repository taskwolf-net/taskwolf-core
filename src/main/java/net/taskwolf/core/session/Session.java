package net.taskwolf.core.session;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.taskwolf.core.database.DatabaseRow;

import java.util.UUID;

@Getter
@Accessors(fluent = true)
@AllArgsConstructor(staticName = "create")
public final class Session {
  public static Session of(DatabaseRow row) {
    return create(row.findCell(0).uuidValue(), row.findCell(1).uuidValue(),
      row.findCell(2).stringValue(), row.findCell(3).stringValue(),
      row.findCell(4).stringValue(), row.findCell(5).stringValue(),
      row.findCell(6).longValue(), row.findCell(7).stringValue(),
      SessionStatus.valueOf(row.findCell(8).stringValue()));
  }

  private final UUID id;
  private final UUID userId;
  private final String devicePlatform;
  private final String ipAddress;
  private final String country;
  private final String city;
  private final long openTime;
  private String lastRefreshToken;
  private SessionStatus status;

  public void updateRefreshToken(String refreshToken) {
    lastRefreshToken = refreshToken;
  }

  public void close() {
    status = SessionStatus.CLOSED;
  }
}
