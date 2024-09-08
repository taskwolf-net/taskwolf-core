package net.taskwolf.core.worker.server.node;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import net.taskwolf.core.event.EventHook;
import net.taskwolf.core.event.Hook;
import net.taskwolf.core.log.Log;
import net.taskwolf.core.worker.WorkerConfiguration;
import net.taskwolf.core.worker.client.WorkerProxyClient;
import net.taskwolf.core.worker.event.node.NodeDisconnectEvent;
import net.taskwolf.core.worker.packet.outgoing.node.PacketOutgoingHandshakeRequest;

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
