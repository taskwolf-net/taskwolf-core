package net.taskwolf.core.worker;

import lombok.Getter;
import lombok.experimental.Accessors;
import net.taskwolf.core.configuration.Configuration;
import org.json.JSONObject;

@Getter
@Accessors(fluent = true)
public final class WorkerConfiguration extends Configuration {
  private static final String CONFIGURATION_PATH = "/configurations/distribution/distribution.json";

  public static WorkerConfiguration createAndLoad() throws Exception {
    var configuration = new WorkerConfiguration(CONFIGURATION_PATH);
    configuration.load();
    return configuration;
  }

  private int restPort;
  private int distributionPort;
  private String proxyHostname;
  private int proxyDistributionPort;
  private String distributionKey;

  private WorkerConfiguration(String path) {
    super(path);
  }

  @Override
  protected void deserialize(JSONObject json) {
    restPort = json.getInt("restPort");
    distributionPort = json.getInt("distributionPort");
    proxyHostname = json.getString("proxyHostname");
    proxyDistributionPort = json.getInt("proxyDistributionPort");
    distributionKey = json.getString("distributionKey");
  }
}
