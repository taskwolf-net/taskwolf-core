package com.dulno.core.stripe;

import com.dulno.core.database.DatabaseRow;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import java.util.UUID;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(staticName = "create")
public final class StripeAccount {
  public static StripeAccount of(DatabaseRow row) {
    return create(row.findCell(0).stringValue(), row.findCell(1).uuidValue(),
      row.findCell(2).uuidValue(), row.findCell(3).stringValue());
  }

  private final String accountId;
  private final UUID targetId;
  private final UUID userId;
  private final String subscriptionId;
}
