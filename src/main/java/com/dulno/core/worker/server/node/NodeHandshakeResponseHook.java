package com.dulno.core.worker.server.node;

import com.dulno.core.event.EventHook;
import com.dulno.core.event.Hook;
import com.dulno.core.worker.event.node.NodeHandshakeResponseEvent;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import com.dulno.core.log.Log;

@Singleton
@RequiredArgsConstructor(access = AccessLevel.PRIVATE, onConstructor = @__({@Inject}))
public final class NodeHandshakeResponseHook implements Hook {
  private final Log log;

  @EventHook
  private void nodeHandshakeResponse(NodeHandshakeResponseEvent event) {
    log.info("Successfully connected to proxy");
  }
}
