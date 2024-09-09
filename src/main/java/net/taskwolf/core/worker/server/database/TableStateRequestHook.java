package net.taskwolf.core.worker.server.database;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import net.taskwolf.core.database.DatabaseKeyspace;
import net.taskwolf.core.database.DatabaseTable;
import net.taskwolf.core.event.EventHook;
import net.taskwolf.core.event.Hook;
import net.taskwolf.core.log.Log;
import net.taskwolf.core.worker.client.WorkerProxyClient;
import net.taskwolf.core.worker.event.database.TableStateRequestEvent;
import net.taskwolf.core.worker.packet.outgoing.database.PacketOutgoingTableStateResponse;

@Singleton
@RequiredArgsConstructor(access = AccessLevel.PRIVATE, onConstructor = @__({@Inject}))
public final class TableStateRequestHook implements Hook {
  private final Log log;
  private final DatabaseKeyspace keyspace;
  private final WorkerProxyClient proxyClient;

  @EventHook
  private void tableStateRequest(TableStateRequestEvent event) {
    var tableOptional = keyspace.findTableByClass(event.tableClass());
    if (tableOptional.isEmpty()) {
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
