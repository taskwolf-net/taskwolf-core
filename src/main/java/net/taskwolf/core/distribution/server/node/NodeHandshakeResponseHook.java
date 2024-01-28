package net.taskwolf.core.distribution.server.node;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import net.taskwolf.core.distribution.client.DistributionClientRegistry;
import net.taskwolf.core.event.EventHook;
import net.taskwolf.core.event.Hook;
import net.taskwolf.core.event.node.NodeHandshakeResponseEvent;

@Singleton
@RequiredArgsConstructor(access = AccessLevel.PRIVATE, onConstructor = @__({@Inject}))
public final class NodeHandshakeResponseHook implements Hook {
  private final DistributionClientRegistry distributionClientRegistry;

  @EventHook
  private void nodeHandshakeResponse(NodeHandshakeResponseEvent event) {
    if (!event.success()) {
      distributionClientRegistry.unregisterClient(event.client());
    }
  }
}
