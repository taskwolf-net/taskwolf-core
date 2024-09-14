package com.dulno.core.worker.server.node;

import com.dulno.core.event.EventHook;
import com.dulno.core.event.Hook;
import com.dulno.core.log.Log;
import com.dulno.core.worker.WorkerConfiguration;
import com.dulno.core.worker.client.WorkerProxyClient;
import com.dulno.core.worker.event.node.NodeDisconnectEvent;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import com.dulno.core.worker.packet.outgoing.node.PacketOutgoingHandshakeRequest;

@Singleton
@RequiredArgsConstructor(access = AccessLevel.PRIVATE, onConstructor = @__({@Inject}))
public final class NodeDisconnectHook implements Hook {
  private final Log log;
  private final WorkerConfiguration configuration;
  private final WorkerProxyClient workerProxyClient;

  @EventHook
  private void nodeDisconnect(NodeDisconnectEvent event) {
    var reason = event.reason();
    if (reason.isConnectionFailed()) {
      log.severe("The connection to the proxy failed");
    } else if (reason.isTimeOut()) {
      log.severe("The connection to the proxy timed out");
    }
    workerProxyClient.connectAsync(() -> workerProxyClient.sendPacket(
      new PacketOutgoingHandshakeRequest(configuration.distributionKey())));
  }
}
