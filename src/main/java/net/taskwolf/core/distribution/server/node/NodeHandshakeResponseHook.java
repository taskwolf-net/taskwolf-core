package net.taskwolf.core.distribution.server.node;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import net.taskwolf.core.distribution.client.DistributionClientRegistry;
import net.taskwolf.core.event.EventHook;
import net.taskwolf.core.event.Hook;
import net.taskwolf.core.event.node.NodeHandshakeResponseEvent;
import net.taskwolf.core.log.Log;

@Singleton
@RequiredArgsConstructor(access = AccessLevel.PRIVATE, onConstructor = @__({@Inject}))
public final class NodeHandshakeResponseHook implements Hook {
  private final DistributionClientRegistry distributionClientRegistry;
  private final Log log;

  @EventHook
  private void nodeHandshakeResponse(NodeHandshakeResponseEvent event) {
    var client = event.client();
    if (!event.success()) {
      distributionClientRegistry.unregisterClient(client);
      return;
    }
    client.authorize();
    client.updateNodeId(event.nodeId());
    client.condition().addMultipleModules(event.loadedModules());
    log.info("Successfully connected to node " + client.node().information());
  }
}
