package net.taskwolf.core.bundle;

import lombok.Getter;
import lombok.experimental.Accessors;
import net.taskwolf.core.configuration.Configuration;
import org.json.JSONObject;

@Getter
@Accessors(fluent = true)
public final class BundlePreset extends Configuration {
  private static final String CONFIGURATION_PATH = "/configurations/bundle/%s.json";

  public static BundlePreset createAndLoad(BundleType type) throws Exception {
    var configuration = new BundlePreset(String.format(CONFIGURATION_PATH,
      type.toString().toLowerCase()), type);
    configuration.load();
    return configuration;
  }

  private final BundleType type;
  private long expiration;
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

  private BundlePreset(String path, BundleType type) {
    super(path);
    this.type = type;
  }

  @Override
  protected void deserialize(JSONObject json) {
    expiration = json.getLong("expiration");
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
