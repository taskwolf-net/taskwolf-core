package net.taskwolf.core.worker.server.node;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import net.taskwolf.core.event.EventHook;
import net.taskwolf.core.event.Hook;
import net.taskwolf.core.worker.event.node.NodePingEvent;
import net.taskwolf.core.worker.packet.outgoing.node.PacketOutgoingPong;

@Singleton
@RequiredArgsConstructor(access = AccessLevel.PRIVATE, onConstructor = @__({@Inject}))
public final class NodePingHook implements Hook {
  @EventHook
  private void nodePing(NodePingEvent event) {
    event.client().sendPacket(new PacketOutgoingPong(event.value()));
  }
}
