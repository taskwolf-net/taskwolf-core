package net.taskwolf.core.worker.server.node;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import net.taskwolf.core.event.EventHook;
import net.taskwolf.core.event.Hook;
import net.taskwolf.core.log.Log;
import net.taskwolf.core.worker.event.node.NodeDisconnectEvent;

@Singleton
@RequiredArgsConstructor(access = AccessLevel.PRIVATE, onConstructor = @__({@Inject}))
public final class NodeDisconnectHook implements Hook {
  private final Log log;

  @EventHook
  private void nodeDisconnect(NodeDisconnectEvent event) {
    var reason = event.reason();
    if (reason.isConnectionFailed()) {
      log.severe("The connection to the proxy failed");
    } else if (reason.isTimeOut()) {
      log.severe("The connection to the proxy timed out");
    }
    System.exit(0);
  }
}
