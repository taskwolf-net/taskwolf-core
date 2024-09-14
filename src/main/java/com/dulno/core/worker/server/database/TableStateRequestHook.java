package com.dulno.core.worker.server.database;

import com.dulno.core.database.DatabaseKeyspace;
import com.dulno.core.event.EventHook;
import com.dulno.core.event.Hook;
import com.dulno.core.log.Log;
import com.dulno.core.worker.client.WorkerProxyClient;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import com.dulno.core.worker.event.database.TableStateRequestEvent;
import com.dulno.core.worker.packet.outgoing.database.PacketOutgoingTableStateResponse;

@Singleton
@RequiredArgsConstructor(access = AccessLevel.PRIVATE, onConstructor = @__({@Inject}))
public final class TableStateRequestHook implements Hook {
  private final Log log;
  private final DatabaseKeyspace keyspace;
  private final WorkerProxyClient proxyClient;

  @EventHook
  private void tableStateRequest(TableStateRequestEvent event) {
    var tableOptional = keyspace.findTableByClass(event.tableClass());
    if (tableOptional.isEmpty() || event.state().isFailure()) {
      return;
    }
    var table = tableOptional.get();
    table.updateTransformationState(event.state());
    proxyClient.sendPacket(new PacketOutgoingTableStateResponse(
      event.tableClass(), event.state()));
    log.info("The transformation of table " + event.tableClass() +
      " has progressed. The new state of the transformation is now " +
      event.state().toString() + ".");
  }
}
