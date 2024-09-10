package net.taskwolf.core.worker.server.database;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import net.taskwolf.core.database.DatabaseKeyspace;
import net.taskwolf.core.database.transformation.DatabaseTransformationState;
import net.taskwolf.core.event.EventHook;
import net.taskwolf.core.event.Hook;
import net.taskwolf.core.log.Log;
import net.taskwolf.core.worker.client.WorkerProxyClient;
import net.taskwolf.core.worker.event.database.TableStateResponseEvent;
import net.taskwolf.core.worker.packet.outgoing.database.PacketOutgoingTableStateRequest;

import java.util.concurrent.CompletableFuture;

@Singleton
@RequiredArgsConstructor(access = AccessLevel.PRIVATE, onConstructor = @__({@Inject}))
public final class TableStateResponseHook implements Hook {
  private final Log log;
  private final DatabaseKeyspace keyspace;
  private final WorkerProxyClient proxyClient;

  @EventHook
  private void tableStateResponse(TableStateResponseEvent event) {
    var tableOptional = keyspace.findTableByClass(event.tableClass());
    if (tableOptional.isEmpty()) {
      return;
    }
    log.info("All pods have started the new state of the transformation for the table " +
      event.tableClass() + ". That is why we are now starting with " +
      event.state() + ".");
    var table = tableOptional.get();
    CompletableFuture<DatabaseTransformationState> futureState;
    if (event.state().isFillTemporary()) {
      futureState = table.fillTemporaryTable();
    } else if (event.state().isUseTemporary()) {
      futureState = table.useTemporaryTable();
    } else if (event.state().isFillNew()) {
      futureState = table.fillNewTable();
    } else if (event.state().isUseNew()) {
      futureState = table.useNewTable();
    } else {
      return;
    }
    futureState.thenAccept(nextState -> publishNextState(event.tableClass(),
      event.state(), nextState));
  }

  private void publishNextState(
    String tableClass, DatabaseTransformationState currentState,
    DatabaseTransformationState nextState
  ) {
    if (nextState.isFailure()) {
      log.severe("An error occurred when moving data from one table to another. " +
        "The error occurred in the " + currentState.toString() +
        " phase of the " + tableClass + " table. The error was generated because " +
        "the size of the original and new table did not match. " +
        "Manual intervention is required!");
      return;
    }
    proxyClient.sendPacket(new PacketOutgoingTableStateRequest(tableClass, nextState));
    log.info("The last transformation phase for the table " + tableClass +
      "has just been successfully completed. Phase " + nextState.toString() +
      " is now initiated.");
  }
}
