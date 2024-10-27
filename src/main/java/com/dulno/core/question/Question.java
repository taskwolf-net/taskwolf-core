package com.dulno.core.question;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;
import com.dulno.core.database.DatabaseRow;

import java.util.UUID;

@Getter
@Accessors(fluent = true)
@AllArgsConstructor(staticName = "create")
public final class Question {
  public static Question of(DatabaseRow row) {
    return create(row.findCell(0).uuidValue(), row.findCell(1).stringValue(),
      row.findCell(2).stringValue(),
      Status.valueOf(row.findCell(3).stringValue()),
      row.findCell(4).longValue());
  }

  public enum Status {
    OPEN,
    CLOSED
  }

  private final UUID id;
  private final String sender;
  private final String title;
  private Status status;
  private long expirationTime;

  public void updateStatus(Status newStatus) {
    status = newStatus;
  }

  private static final long EXPIRATION_TIME = 1000 * 60 * 60 * 24 * 14;

  public void resetExpirationTime() {
    expirationTime = System.currentTimeMillis() + EXPIRATION_TIME;
  }

  public void disableExpirationTime() {
    expirationTime = -1;
  }
}
