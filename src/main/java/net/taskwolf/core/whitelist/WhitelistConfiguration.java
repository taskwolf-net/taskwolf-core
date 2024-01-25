package net.taskwolf.core.whitelist;

import lombok.Getter;
import lombok.experimental.Accessors;
import net.taskwolf.core.configuration.Configuration;
import org.json.JSONObject;

@Getter
@Accessors(fluent = true)
public final class WhitelistConfiguration extends Configuration {
  private static final String CONFIGURATION_PATH = "/configurations/whitelist/whitelist.json";

  public static WhitelistConfiguration createAndLoad() throws Exception {
    var configuration = new WhitelistConfiguration(CONFIGURATION_PATH);
    configuration.load();
    return configuration;
  }

  private boolean whitelistEnabled;
  private String whitelistKey;

  private WhitelistConfiguration(String path) {
    super(path);
  }

  @Override
  protected void deserialize(JSONObject json) {
    whitelistEnabled = json.getBoolean("whitelistEnabled");
    whitelistKey = json.getString("whitelistKey");
  }
}
