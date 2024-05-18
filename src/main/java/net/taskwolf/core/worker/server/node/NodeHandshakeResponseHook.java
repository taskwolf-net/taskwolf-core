package net.taskwolf.core.worker.server.node;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import net.taskwolf.core.event.EventHook;
import net.taskwolf.core.event.Hook;
import net.taskwolf.core.log.Log;
import net.taskwolf.core.worker.event.node.NodeHandshakeResponseEvent;

@Singleton
@RequiredArgsConstructor(access = AccessLevel.PRIVATE, onConstructor = @__({@Inject}))
public final class NodeHandshakeResponseHook implements Hook {
  private final Log log;

  @EventHook
  private void nodeHandshakeResponse(NodeHandshakeResponseEvent event) {
    log.info("Successfully connected to proxy");
  }
}
