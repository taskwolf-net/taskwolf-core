package net.taskwolf.core.distribution;

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.taskwolf.core.configuration.Configuration;
import org.json.JSONObject;

import java.util.List;

@Getter
@Accessors(fluent = true)
public final class DistributionConfiguration extends Configuration {
  private static final String CONFIGURATION_PATH = "/configurations/distribution/distribution.json";

  public static DistributionConfiguration createAndLoad() throws Exception {
    var configuration = new DistributionConfiguration(CONFIGURATION_PATH);
    configuration.load();
    return configuration;
  }

  private Node self;
  private List<Node> nodes;

  private DistributionConfiguration(String path) {
    super(path);
  }

  @Override
  protected void deserialize(JSONObject json) {
    self = createNode(json.getJSONObject("self"));
    nodes = Lists.newArrayList();
    var nodesJson = json.getJSONArray("nodes");
    for (var i = 0; i < nodesJson.length(); i++) {
      nodes.add(createNode(nodesJson.getJSONObject(i)));
    }
  }

  private Node createNode(JSONObject json) {
    return Node.create(json.getString("hostname"), json.getInt("redisPort"),
      json.getInt("restPort"));
  }
}
