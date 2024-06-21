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
  private long workflowExecutionLimit;
  private boolean workflowTemplateAccess;
  private boolean databaseAccess;
  private long databaseNumberLimit;
  private long databaseDataLimit;
  private boolean webhookAccess;
  private long webhookNumberLimit;
  private boolean organizationAccess;
  private long organizationMemberLimit;
  private boolean deviceAccess;
  private long deviceNumberLimit;
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
    workflowExecutionLimit = json.getLong("workflowExecutionLimit");
    workflowTemplateAccess = json.getBoolean("workflowTemplateAccess");
    databaseAccess = json.getBoolean("databaseAccess");
    databaseNumberLimit = json.getLong("databaseNumberLimit");
    databaseDataLimit = json.getLong("databaseDataLimit");
    webhookAccess = json.getBoolean("webhookAccess");
    webhookNumberLimit = json.getLong("webhookNumberLimit");
    organizationAccess = json.getBoolean("organizationAccess");
    organizationMemberLimit = json.getLong("organizationMemberLimit");
    deviceAccess = json.getBoolean("deviceAccess");
    deviceNumberLimit = json.getLong("deviceNumberLimit");
    accountsAccess = json.getBoolean("accountsAccess");
    accountsNumberLimit = json.getLong("accountsNumberLimit");
  }
}
