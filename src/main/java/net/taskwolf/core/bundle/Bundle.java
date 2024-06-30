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
      BundleClass.valueOf(row.findCell(2).stringValue()),
      BundleRuntime.valueOf(row.findCell(3).stringValue()),
      row.findCell(4).doubleValue(), row.findCell(5).longValue(),
      row.findCell(6).booleanValue(), row.findCell(7).longValue(),
      row.findCell(8).longValue(), row.findCell(9).booleanValue(),
      row.findCell(10).booleanValue(), row.findCell(11).longValue(),
      row.findCell(12).doubleValue(), row.findCell(13).booleanValue(),
      row.findCell(14).longValue(), row.findCell(15).booleanValue(),
      row.findCell(16).longValue(), row.findCell(17).booleanValue(),
      row.findCell(18).booleanValue(), row.findCell(19).longValue());
  }

  public static Bundle of(
    UUID ownerId, BundlePreset preset, BundleRuntime runtime
  ) {
    return of(ownerId, preset, runtime, preset.workflowOperationLimit(),
      preset.databaseDataLimit());
  }

  public static Bundle of(
    UUID ownerId, BundlePreset preset, BundleRuntime runtime,
    long workflowOperationLimit, double databaseDataLimit
  ) {
    var expiration = System.currentTimeMillis() + (runtime.isMonthly() ?
      1000L * 60 * 60 * 24 * 30 : 1000L * 60 * 60 * 24 * 365);
    return create(ownerId, preset.bundleType(), preset.bundleClass(), runtime,
      runtime.isMonthly() ? preset.monthlyPrice() : preset.yearlyPrice(),
      expiration, preset.workflowAccess(), preset.workflowNumberLimit(),
      workflowOperationLimit, preset.workflowTemplateAccess(),
      preset.databaseAccess(), preset.databaseNumberLimit(),
      databaseDataLimit, preset.webhookAccess(), preset.webhookNumberLimit(),
      preset.organizationAccess(), preset.organizationMemberLimit(),
      preset.deviceAccess(), preset.accountsAccess(), preset.accountsNumberLimit());
  }

  private final UUID ownerId;
  private final BundleType bundleType;
  private final BundleClass bundleClass;
  private final BundleRuntime bundleRuntime;
  private final double price;
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
