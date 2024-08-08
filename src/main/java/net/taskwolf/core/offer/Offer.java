package net.taskwolf.core.offer;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.taskwolf.core.bundle.BundleClass;
import net.taskwolf.core.bundle.BundleRuntime;
import net.taskwolf.core.bundle.BundleType;
import net.taskwolf.core.database.DatabaseRow;

import java.util.UUID;

@Getter
@Accessors(fluent = true)
@AllArgsConstructor(staticName = "create")
public final class Offer {
  public static Offer of(DatabaseRow row) {
    return create(row.findCell(0).uuidValue(), row.findCell(1).uuidValue(),
      OfferStatus.valueOf(row.findCell(2).stringValue()),
      BundleType.valueOf(row.findCell(3).stringValue()),
      BundleClass.valueOf(row.findCell(4).stringValue()),
      BundleRuntime.valueOf(row.findCell(5).stringValue()),
      row.findCell(6).doubleValue(), row.findCell(7).booleanValue(),
      row.findCell(8).longValue(), row.findCell(9).longValue(),
      row.findCell(10).booleanValue(), row.findCell(11).booleanValue(),
      row.findCell(12).longValue(), row.findCell(13).doubleValue(),
      row.findCell(14).booleanValue(), row.findCell(15).longValue(),
      row.findCell(16).booleanValue(), row.findCell(17).longValue(),
      row.findCell(18).longValue(), row.findCell(19).booleanValue(),
      row.findCell(20).booleanValue(), row.findCell(21).longValue());
  }

  private final UUID id;
  private final UUID targetId;
  private OfferStatus offerStatus;
  private final BundleType bundleType;
  private final BundleClass bundleClass;
  private final BundleRuntime bundleRuntime;
  private final double price;
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

  public void updateStatus(OfferStatus newStatus) {
    offerStatus = newStatus;
  }
}
