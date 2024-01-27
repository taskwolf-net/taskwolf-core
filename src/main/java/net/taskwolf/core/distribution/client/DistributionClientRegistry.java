package net.taskwolf.core.distribution.client;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import net.taskwolf.core.distribution.NodeType;

import java.util.List;

@Singleton
@RequiredArgsConstructor(access = AccessLevel.PRIVATE, onConstructor = @__({@Inject}))
public final class DistributionClientRegistry {
  private final List<DistributionClient> clients = Lists.newArrayList();

  public void registerClient(DistributionClient client) {
    clients.add(client);
  }

  public void unregisterClient(DistributionClient client) {
    clients.remove(client);
  }

  public List<DistributionClient> findClientsByType(NodeType type) {
    return clients.stream().filter(client -> client.node().type().equals(type))
      .toList();
  }

  public List<DistributionClient> findAllClients() {
    return List.copyOf(clients);
  }
}

