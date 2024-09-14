package com.dulno.core.worker.server.node;

import com.dulno.core.worker.event.node.NodePingEvent;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import com.dulno.core.event.EventHook;
import com.dulno.core.event.Hook;
import com.dulno.core.worker.packet.outgoing.node.PacketOutgoingPong;

@Singleton
@RequiredArgsConstructor(access = AccessLevel.PRIVATE, onConstructor = @__({@Inject}))
public final class NodePingHook implements Hook {
  @EventHook
  private void nodePing(NodePingEvent event) {
    event.client().sendPacket(new PacketOutgoingPong(event.value()));
  }
}
