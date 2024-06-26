package net.taskwolf.core.bundle;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.taskwolf.core.database.DatabaseRow;

import java.util.UUID;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(staticName = "create")
public final class Bundle {
  public static Bundle of(DatabaseRow row) {
    return create(row.findCell(0).uuidValue(),
      BundleType.valueOf(row.findCell(1).stringValue()),
      row.findCell(2).longValue(), row.findCell(3).booleanValue(),
      row.findCell(4).longValue(), row.findCell(5).longValue(),
      row.findCell(6).booleanValue(), row.findCell(7).booleanValue(),
      row.findCell(8).longValue(), row.findCell(9).doubleValue(),
      row.findCell(10).booleanValue(), row.findCell(11).longValue(),
      row.findCell(12).booleanValue(), row.findCell(13).longValue(),
      row.findCell(14).booleanValue(), row.findCell(15).booleanValue(),
      row.findCell(16).longValue());
  }

  public static Bundle of(UUID ownerId, BundlePreset preset, long expiration) {
    return of(ownerId, preset, expiration, preset.workflowOperationLimit(),
      preset.databaseDataLimit());
  }

  public static Bundle of(
    UUID ownerId, BundlePreset preset, long expiration,
    long workflowOperationLimit, double databaseDataLimit
  ) {
    return create(ownerId, preset.type(), expiration,
      preset.workflowAccess(), preset.workflowNumberLimit(),
      workflowOperationLimit, preset.workflowTemplateAccess(),
      preset.databaseAccess(), preset.databaseNumberLimit(),
      databaseDataLimit, preset.webhookAccess(), preset.webhookNumberLimit(),
      preset.organizationAccess(), preset.organizationMemberLimit(),
      preset.deviceAccess(), preset.accountsAccess(), preset.accountsNumberLimit());
  }

  private final UUID ownerId;
  private final BundleType type;
  private final long expiration;
  private final boolean workflowAccess;
  private final long workflowNumberLimit;
  private final long workflowOperationLimit;
  private final boolean workflowTemplateAccess;
  private final boolean databaseAccess;
  private final long databaseNumberLimit;
  private final double databaseDataLimit;
  private final boolean webhookAccess;
  private final long webhookNumberLimit;
  private final boolean organizationAccess;
  private final long organizationMemberLimit;
  private final boolean deviceAccess;
  private final boolean accountsAccess;
  private final long accountsNumberLimit;
}
