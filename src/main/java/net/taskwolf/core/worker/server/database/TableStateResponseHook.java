package net.taskwolf.core.worker.server.database;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import net.taskwolf.core.database.DatabaseKeyspace;
import net.taskwolf.core.database.transformation.DatabaseTransformationState;
import net.taskwolf.core.event.EventHook;
import net.taskwolf.core.event.Hook;
import net.taskwolf.core.worker.client.WorkerProxyClient;
import net.taskwolf.core.worker.event.database.TableStateRequestEvent;
import net.taskwolf.core.worker.packet.outgoing.database.PacketOutgoingTableStateRequest;

import java.util.concurrent.CompletableFuture;

@Singleton
@RequiredArgsConstructor(access = AccessLevel.PRIVATE, onConstructor = @__({@Inject}))
public final class TableStateResponseHook implements Hook {
  private final DatabaseKeyspace keyspace;
  private final WorkerProxyClient proxyClient;

  @EventHook
  private void tableStateResponse(TableStateRequestEvent event) {
    var tableOptional = keyspace.findTableByClass(event.tableClass());
    if (tableOptional.isEmpty()) {
      return;
    }
    var table = tableOptional.get();
    CompletableFuture<DatabaseTransformationState> futureState;
    if (event.state().isFillTemporary()) {
      futureState = table.fillTemporaryTable();
    } else if (event.state().isUseTemporary()) {
      futureState = table.useNewTable();
    } else if (event.state().isFillNew()) {
      futureState = table.fillNewTable();
    } else if (event.state().isUseNew()) {
      futureState = table.useNewTable();
    } else {
      return;
    }
    futureState.thenAccept(nextState -> proxyClient.sendPacket(
      new PacketOutgoingTableStateRequest(event.tableClass(), nextState)));
  }
}
