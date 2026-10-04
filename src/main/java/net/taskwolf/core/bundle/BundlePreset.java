package net.taskwolf.core.bundle;

import net.taskwolf.core.configuration.Configuration;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.json.JSONObject;

@Getter
@Accessors(fluent = true)
public final class BundlePreset extends Configuration {
  private static final String CONFIGURATION_PATH = "/configurations/bundle/%s.json";

  public static BundlePreset createAndLoad(
    BundleType bundleType, BundleClass bundleClass
  ) throws Exception {
    var configuration = new BundlePreset(String.format(CONFIGURATION_PATH,
      bundleType.toString().toLowerCase() + "-" + bundleClass.toString().toLowerCase()),
      bundleType, bundleClass);
    configuration.load();
    return configuration;
  }

  public static BundlePreset createAndLoad(BundleType bundleType) throws Exception {
    var configuration = new BundlePreset(String.format(CONFIGURATION_PATH,
      bundleType.toString().toLowerCase()), bundleType, BundleClass.NONE);
    configuration.load();
    return configuration;
  }

  private final BundleType bundleType;
  private final BundleClass bundleClass;
  private boolean hasPrice;
  private double monthlyPrice;
  private double yearlyPrice;
  private boolean workflowAccess;
  private boolean hasWorkflowNumberLimit;
  private long workflowNumberLimit;
  private boolean hasWorkflowOperationLimit;
  private long workflowOperationLimit;
  private boolean workflowTemplateAccess;
  private boolean processAccess;
  private boolean hasProcessNumberLimit;
  private long processNumberLimit;
  private boolean databaseAccess;
  private boolean hasDatabaseNumberLimit;
  private long databaseNumberLimit;
  private boolean hasDatabaseDataLimit;
  private double databaseDataLimit;
  private boolean webhookAccess;
  private long webhookNumberLimit;
  private boolean organizationAccess;
  private boolean hasOrganizationLimits;
  private long organizationMemberLimit;
  private long organizationTeamLimit;
  private boolean deviceAccess;
  private boolean accountsAccess;
  private long accountsNumberLimit;

  private BundlePreset(
    String path, BundleType bundleType, BundleClass bundleClass
  ) {
    super(path);
    this.bundleType = bundleType;
    this.bundleClass = bundleClass;
  }

  @Override
  protected void deserialize(JSONObject json) {
    hasPrice = json.has("monthlyPrice") && json.has("yearlyPrice");
    if (hasPrice) {
      monthlyPrice = json.getDouble("monthlyPrice");
      yearlyPrice = json.getDouble("yearlyPrice");
    }
    workflowAccess = json.getBoolean("workflowAccess");
    hasWorkflowNumberLimit = json.has("workflowNumberLimit");
    if (hasWorkflowNumberLimit) {
      workflowNumberLimit = json.getLong("workflowNumberLimit");
    }
    hasWorkflowOperationLimit = json.has("workflowOperationLimit");
    if (hasWorkflowOperationLimit) {
      workflowOperationLimit = json.getLong("workflowOperationLimit");
    }
    workflowTemplateAccess = json.getBoolean("workflowTemplateAccess");
    processAccess = json.getBoolean("processAccess");
    hasProcessNumberLimit = json.has("processNumberLimit");
    if (hasProcessNumberLimit) {
      processNumberLimit = json.getLong("processNumberLimit");
    }
    databaseAccess = json.getBoolean("databaseAccess");
    hasDatabaseNumberLimit = json.has("databaseNumberLimit");
    if (hasDatabaseNumberLimit) {
      databaseNumberLimit = json.getLong("databaseNumberLimit");
    }
    hasDatabaseDataLimit = json.has("databaseDataLimit");
    if (hasDatabaseDataLimit) {
      databaseDataLimit = json.getDouble("databaseDataLimit");
    }
    webhookAccess = json.getBoolean("webhookAccess");
    webhookNumberLimit = json.getLong("webhookNumberLimit");
    organizationAccess = json.getBoolean("organizationAccess");
    hasOrganizationLimits = json.has("organizationMemberLimit") &&
      json.has("organizationTeamLimit");
    if (hasOrganizationLimits) {
      organizationMemberLimit = json.getLong("organizationMemberLimit");
      organizationTeamLimit = json.getLong("organizationTeamLimit");
    }
    deviceAccess = json.getBoolean("deviceAccess");
    accountsAccess = json.getBoolean("accountsAccess");
    accountsNumberLimit = json.getLong("accountsNumberLimit");
  }
}
