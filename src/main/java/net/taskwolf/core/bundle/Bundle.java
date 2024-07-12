package net.taskwolf.core.bundle;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.taskwolf.core.database.DatabaseRow;

import java.util.UUID;

@Getter
@Accessors(fluent = true)
@AllArgsConstructor(staticName = "create")
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
      row.findCell(16).longValue(), row.findCell(17).longValue(),
      row.findCell(18).booleanValue(), row.findCell(19).booleanValue(),
      row.findCell(20).longValue());
  }

  public static Bundle of(
    UUID ownerId, BundlePreset preset, BundleRuntime runtime
  ) {
    return of(ownerId, preset, runtime, calculatePresetPrice(preset, runtime),
      preset.workflowOperationLimit(), preset.databaseDataLimit(),
      preset.organizationMemberLimit(), preset.organizationTeamLimit());
  }

  private static double calculatePresetPrice(
    BundlePreset preset, BundleRuntime runtime
  ) {
    if (preset.bundleType().isTrial()) {
      return 0;
    }
    return runtime.isMonthly() ? preset.monthlyPrice() : preset.yearlyPrice();
  }

  public static Bundle of(
    UUID ownerId, BundlePreset preset, BundleRuntime runtime,
    double price, long workflowOperationLimit, double databaseDataLimit,
    long organizationMemberLimit, long organizationTeamLimit
  ) {
    return create(ownerId, preset.bundleType(), preset.bundleClass(), runtime,
      price, calculateBundleExpiration(runtime), preset.workflowAccess(),
      preset.workflowNumberLimit(), workflowOperationLimit,
      preset.workflowTemplateAccess(), preset.databaseAccess(),
      preset.databaseNumberLimit(), databaseDataLimit, preset.webhookAccess(),
      preset.webhookNumberLimit(), preset.organizationAccess(),
      organizationMemberLimit, organizationTeamLimit,
      preset.deviceAccess(), preset.accountsAccess(), preset.accountsNumberLimit());
  }

  private static long calculateBundleExpiration(BundleRuntime runtime) {
    return System.currentTimeMillis() + 1000L * 60 * 60 * 24 * switch (runtime) {
      case WEEKLY -> 7;
      case MONTHLY -> 30;
      case YEARLY -> 365;
    };
  }

  private final UUID ownerId;
  private final BundleType bundleType;
  private final BundleClass bundleClass;
  private final BundleRuntime bundleRuntime;
  private final double price;
  private long expiration;
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
  private final long organizationTeamLimit;
  private final boolean deviceAccess;
  private final boolean accountsAccess;
  private final long accountsNumberLimit;

  public void extend() {
    expiration = calculateBundleExpiration(bundleRuntime);
  }
}
