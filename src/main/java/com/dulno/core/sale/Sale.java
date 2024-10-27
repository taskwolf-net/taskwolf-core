package com.dulno.core.sale;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;
import com.dulno.core.database.DatabaseRow;

import java.util.UUID;

@Getter
@Accessors(fluent = true)
@AllArgsConstructor(staticName = "create")
public final class Sale {
  public static Sale of(DatabaseRow row) {
    return create(row.findCell(0).uuidValue(), row.findCell(1).stringValue(),
      row.findCell(2).stringValue(), row.findCell(3).stringValue(),
      row.findCell(4).stringValue(), row.findCell(5).stringValue(),
      row.findCell(6).stringValue(), row.findCell(7).stringValue(),
      row.findCell(8).stringValue(), row.findCell(9).stringValue(),
      Status.valueOf(row.findCell(10).stringValue()),
      row.findCell(11).longValue());
  }

  public enum Status {
    OPEN,
    CLOSED
  }

  private final UUID id;
  private final String sender;
  private final String firstName;
  private final String lastName;
  private final String phoneNumber;
  private final String country;
  private final String companyName;
  private final String companySize;
  private final String companyRole;
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
