package net.taskwolf.core.stripe;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.taskwolf.core.database.DatabaseRow;

import java.util.UUID;

@Getter
@Accessors(fluent = true)
@AllArgsConstructor(staticName = "create")
public final class StripeAccount {
  public static StripeAccount of(DatabaseRow row) {
    return create(row.findCell(0).uuidValue(), row.findCell(1).stringValue(),
      row.findCell(2).stringValue());
  }

  private final UUID userId;
  private final String accountId;
  private String subscriptionId;

  public void updateSubscription(String newSubscriptionId) {
    subscriptionId = newSubscriptionId;
  }
}
