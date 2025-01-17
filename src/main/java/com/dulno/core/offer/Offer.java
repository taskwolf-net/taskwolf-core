package com.dulno.core.offer;

import com.dulno.core.bundle.*;
import com.dulno.core.database.DatabaseRow;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.time.ZonedDateTime;
import java.util.UUID;

@Getter
@Accessors(fluent = true)
@AllArgsConstructor(staticName = "create")
public final class Offer {
  public static Offer of(DatabaseRow row) {
    return create(row.findCell(0).uuidValue(), row.findCell(1).uuidValue(),
      row.findCell(2).stringValue(),
      OfferStatus.valueOf(row.findCell(3).stringValue()),
      BundleType.valueOf(row.findCell(4).stringValue()),
      BundleClass.valueOf(row.findCell(5).stringValue()),
      BundleRuntime.valueOf(row.findCell(6).stringValue()),
      row.findCell(7).doubleValue(), row.findCell(8).booleanValue(),
      row.findCell(9).longValue(), row.findCell(10).longValue(),
      row.findCell(11).booleanValue(), row.findCell(12).booleanValue(),
      row.findCell(13).longValue(), row.findCell(14).booleanValue(),
      row.findCell(15).longValue(), row.findCell(16).doubleValue(),
      row.findCell(17).booleanValue(), row.findCell(18).longValue(),
      row.findCell(19).booleanValue(), row.findCell(20).longValue(),
      row.findCell(21).longValue(), row.findCell(22).booleanValue(),
      row.findCell(23).booleanValue(), row.findCell(24).longValue());
  }

  public static Offer of(
    UUID id, UUID targetId, String priceId, OfferStatus offerStatus,
    BundlePreset preset, BundleRuntime runtime, double price,
    long workflowNumberLimit, long workflowOperationLimit, long processNumberLimit,
    long databaseNumberLimit, double databaseDataLimit,
    long organizationMemberLimit, long organizationTeamLimit
  ) {
    return create(id, targetId, priceId, offerStatus, preset.bundleType(),
      preset.bundleClass(), runtime, price, preset.workflowAccess(),
      workflowNumberLimit, workflowOperationLimit,
      preset.workflowTemplateAccess(), preset.processAccess(), processNumberLimit,
      preset.databaseAccess(), databaseNumberLimit, databaseDataLimit,
      preset.webhookAccess(), preset.webhookNumberLimit(),
      preset.organizationAccess(), organizationMemberLimit, organizationTeamLimit,
      preset.deviceAccess(), preset.accountsAccess(), preset.accountsNumberLimit());
  }

  private final UUID id;
  private final UUID targetId;
  private final String priceId;
  private OfferStatus offerStatus;
  private final BundleType bundleType;
  private final BundleClass bundleClass;
  private final BundleRuntime bundleRuntime;
  private final double price;
  private final boolean workflowAccess;
  private final long workflowNumberLimit;
  private final long workflowOperationLimit;
  private final boolean workflowTemplateAccess;
  private final boolean processAccess;
  private final long processNumberLimit;
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

  public void updateStatus(OfferStatus newStatus) {
    offerStatus = newStatus;
  }

  public Bundle toBundle() {
    return Bundle.create(targetId, bundleType, bundleClass, bundleRuntime, price,
      calculateBundleExpiration(bundleRuntime), workflowAccess, workflowNumberLimit,
      workflowOperationLimit, workflowTemplateAccess, processAccess,
      processNumberLimit, databaseAccess, databaseNumberLimit, databaseDataLimit,
      webhookAccess, webhookNumberLimit, organizationAccess,
      organizationMemberLimit, organizationTeamLimit, deviceAccess,
      accountsAccess, accountsNumberLimit);
  }

  private long calculateBundleExpiration(BundleRuntime runtime) {
    if (runtime.isUnbound()) {
      return 0;
    }
    if (runtime.isInfinite()) {
      return -1;
    }
    var current = ZonedDateTime.now();
    var next = current.plusMonths(runtime.isMonthly() ? 1 : 12);
    if (next.getDayOfMonth() != current.getDayOfMonth()) {
      next = next.withDayOfMonth(next.getMonth().length(
        next.toLocalDate().isLeapYear()));
    }
    return next.toInstant().toEpochMilli();
  }
}
