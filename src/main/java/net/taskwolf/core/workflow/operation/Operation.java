package net.taskwolf.core.workflow.operation;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.taskwolf.core.database.DatabaseRow;

import java.util.UUID;

@Getter
@Accessors(fluent = true)
@AllArgsConstructor(staticName = "create")
public final class Operation {
  public static Operation of(DatabaseRow row) {
    return create(row.findCell(0).uuidValue(), row.findCell(1).longValue(),
      row.findCell(2).longValue());
  }

  private final UUID targetId;
  private long operations;
  private long expiration;

  public void addOperations(long additionalOperations) {
    operations += additionalOperations;
  }

  public void extendExpiration() {
    operations = 0;
    expiration = expiration + 1000L * 60 * 60 * 24 * 30;
  }

  public void resetExpiration() {
    operations = 0;
    expiration = System.currentTimeMillis() + 1000L * 60 * 60 * 24 * 30;
  }
}
