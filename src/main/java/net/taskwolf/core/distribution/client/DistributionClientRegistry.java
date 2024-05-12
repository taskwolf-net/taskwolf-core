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

  /**
   * Registers a new distribution client
   * @param client The new distribution client
   */
  public void registerClient(DistributionClient client) {
    clients.add(client);
  }


  /**
   * Unregisters a distribution client
   * @param client The client that is to be unregistered
   */
  public void unregisterClient(DistributionClient client) {
    clients.remove(client);
  }

  /**
   * Is used to find a list of distribution clients by node type
   * @param type The type of the node of the client
   * @return The list of all registered clients with this node type
   */
  public List<DistributionClient> findClientsByType(NodeType type) {
    return clients.stream().filter(client -> client.node().type().equals(type))
      .toList();
  }

  /**
   * Is used to find all registered distribution clients
   * @return The list of all distribution clients
   */
  public List<DistributionClient> findAllClients() {
    return List.copyOf(clients);
  }
}

