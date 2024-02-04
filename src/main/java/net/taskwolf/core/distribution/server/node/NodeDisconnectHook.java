package net.taskwolf.core.distribution.server.node;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import net.taskwolf.core.distribution.Distribution;
import net.taskwolf.core.distribution.client.DistributionClient;
import net.taskwolf.core.distribution.client.DistributionClientRegistry;
import net.taskwolf.core.distribution.server.DistributionServer;
import net.taskwolf.core.event.EventHook;
import net.taskwolf.core.event.Hook;
import net.taskwolf.core.event.node.NodeDisconnectEvent;
import net.taskwolf.core.log.Log;

import java.util.UUID;

@RequiredArgsConstructor(staticName = "create")
public final class NodeDisconnectHook implements Hook {
  private final Distribution distribution;
  private final DistributionClientRegistry distributionClientRegistry;
  private final DistributionServer distributionServer;
  private final Log log;

  @EventHook
  private void nodeDisconnect(NodeDisconnectEvent event) {
    var client = event.client();
    distributionClientRegistry.unregisterClient(client);
    if (shouldReorganizeUsers()) {
      distribution.findLoadedModules().forEach(distribution::reorganizeUsers);
    }
    var reason = event.reason();
    if (reason.isConnectionFailed()) {
      return;
    }
    var nodeInformation = client.node().information();
    if (reason.isShutdown()) {
      log.info("The node " + nodeInformation + " has disconnected");
    } else {
      log.warning("The node " + nodeInformation + " has timed out");
    }
  }

  private boolean shouldReorganizeUsers() {
    var nodes = distributionClientRegistry.findAllClients().stream()
      .map(DistributionClient::nodeId).toList();
    var ownId = distributionServer.nodeId();
    nodes.add(ownId);
    nodes.sort(UUID::compareTo);
    return nodes.get(0).equals(ownId);
  }
}
