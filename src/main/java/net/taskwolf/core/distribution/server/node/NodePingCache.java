package net.taskwolf.core.distribution.server.node;

import com.datastax.oss.driver.shaded.guava.common.collect.Maps;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import net.taskwolf.core.distribution.client.DistributionClient;

import java.util.List;
import java.util.Map;

@Singleton
@RequiredArgsConstructor(access = AccessLevel.PRIVATE, onConstructor = @__({@Inject}))
public final class NodePingCache {
  private final Map<DistributionClient, Integer> pings = Maps.newHashMap();

  public void insertNodePing(DistributionClient client, int value) {
    pings.put(client, value);
  }

  public void removeNodePing(DistributionClient client) {
    pings.remove(client);
  }

  public void clear() {
    pings.clear();
  }

  public boolean pingExists(DistributionClient client) {
    return pings.containsKey(client);
  }

  public int findPingValue(DistributionClient client) {
    return pings.get(client);
  }

  public List<DistributionClient> findPendingPingClients() {
    return Lists.newArrayList(pings.keySet());
  }
}
