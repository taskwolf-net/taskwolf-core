package net.taskwolf.core.distribution.server.node;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import net.taskwolf.core.event.EventHook;
import net.taskwolf.core.event.Hook;
import net.taskwolf.core.event.node.NodePongEvent;

@Singleton
@RequiredArgsConstructor(access = AccessLevel.PRIVATE, onConstructor = @__({@Inject}))
public final class NodePongHook implements Hook {
  private final NodePingCache pingCache;

  @EventHook
  private void nodePong(NodePongEvent event) {
    var client = event.client();
    if (!pingCache.pingExists(client)) {
      return;
    }
    if (pingCache.findPingValue(client) == event.value()) {
      pingCache.removeNodePing(client);
    }
  }
}
