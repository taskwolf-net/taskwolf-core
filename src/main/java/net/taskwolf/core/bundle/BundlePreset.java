package net.taskwolf.core.bundle;

import lombok.Getter;
import lombok.experimental.Accessors;
import net.taskwolf.core.configuration.Configuration;
import org.json.JSONObject;

@Getter
@Accessors(fluent = true)
public final class BundlePreset extends Configuration {
  private static final String CONFIGURATION_PATH = "/configurations/bundle/%s-%s.json";

  public static BundlePreset createAndLoad(
    BundleType bundleType, BundleClass bundleClass
  ) throws Exception {
    var configuration = new BundlePreset(String.format(CONFIGURATION_PATH,
      bundleType.toString().toLowerCase(), bundleClass.toString().toLowerCase()),
      bundleType, bundleClass);
    configuration.load();
    return configuration;
  }

  private final BundleType bundleType;
  private final BundleClass bundleClass;
  private double monthlyPrice;
  private double yearlyPrice;
  private boolean workflowAccess;
  private long workflowNumberLimit;
  private long workflowOperationLimit;
  private boolean workflowTemplateAccess;
  private boolean databaseAccess;
  private long databaseNumberLimit;
  private double databaseDataLimit;
  private boolean webhookAccess;
  private long webhookNumberLimit;
  private boolean organizationAccess;
  private long organizationMemberLimit;
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
    monthlyPrice = json.getDouble("monthlyPrice");
    yearlyPrice = json.getDouble("yearlyPrice");
    workflowAccess = json.getBoolean("workflowAccess");
    workflowNumberLimit = json.getLong("workflowNumberLimit");
    workflowOperationLimit = json.getLong("workflowOperationLimit");
    workflowTemplateAccess = json.getBoolean("workflowTemplateAccess");
    databaseAccess = json.getBoolean("databaseAccess");
    databaseNumberLimit = json.getLong("databaseNumberLimit");
    databaseDataLimit = json.getDouble("databaseDataLimit");
    webhookAccess = json.getBoolean("webhookAccess");
    webhookNumberLimit = json.getLong("webhookNumberLimit");
    organizationAccess = json.getBoolean("organizationAccess");
    organizationMemberLimit = json.getLong("organizationMemberLimit");
    deviceAccess = json.getBoolean("deviceAccess");
    accountsAccess = json.getBoolean("accountsAccess");
    accountsNumberLimit = json.getLong("accountsNumberLimit");
  }
}
