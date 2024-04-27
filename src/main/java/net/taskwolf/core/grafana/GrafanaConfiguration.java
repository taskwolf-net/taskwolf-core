package net.taskwolf.core.grafana;

import lombok.Getter;
import lombok.experimental.Accessors;
import net.taskwolf.core.configuration.Configuration;
import org.json.JSONObject;

@Getter
@Accessors(fluent = true)
public final class GrafanaConfiguration extends Configuration {
  private static final String CONFIGURATION_PATH = "/configurations/grafana/grafana.json";

  public static GrafanaConfiguration createAndLoad() throws Exception {
    var configuration = new GrafanaConfiguration(CONFIGURATION_PATH);
    configuration.load();
    return configuration;
  }

  private String adminName;
  private String adminPassword;
  private String dashboard;

  private GrafanaConfiguration(String path) {
    super(path);
  }

  @Override
  protected void deserialize(JSONObject json) {
    adminName = json.getString("adminName");
    adminPassword = json.getString("adminPassword");
    dashboard = json.getString("dashboard");
  }
}

