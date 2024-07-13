package net.taskwolf.core.access;

import lombok.Getter;
import lombok.experimental.Accessors;
import net.taskwolf.core.configuration.Configuration;
import org.json.JSONObject;

@Accessors(fluent = true)
public final class VerificationConfiguration extends Configuration {
  private static final String CONFIGURATION_PATH = "/configurations/verification/verification.json";

  public static VerificationConfiguration createAndLoad() throws Exception {
    var configuration = new VerificationConfiguration(CONFIGURATION_PATH);
    configuration.load();
    return configuration;
  }

  @Getter
  private String homeSecret;
  @Getter
  private String productSecret;

  private VerificationConfiguration(String path) {
    super(path);
  }

  @Override
  protected void deserialize(JSONObject json) {
    homeSecret = json.getString("homeSecret");
    productSecret = json.getString("productSecret");
  }
}
