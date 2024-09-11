package net.taskwolf.core.worker.server.database;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import net.taskwolf.core.database.DatabaseKeyspace;
import net.taskwolf.core.database.DatabaseTable;
import net.taskwolf.core.database.transformation.DatabaseTransformationState;
import net.taskwolf.core.event.EventHook;
import net.taskwolf.core.event.Hook;
import net.taskwolf.core.log.Log;
import net.taskwolf.core.worker.client.WorkerProxyClient;
import net.taskwolf.core.worker.event.database.TableTransformEvent;
import net.taskwolf.core.worker.packet.outgoing.database.PacketOutgoingTableStateRequest;

import java.util.concurrent.CompletableFuture;

@Singleton
@RequiredArgsConstructor(access = AccessLevel.PRIVATE, onConstructor = @__({@Inject}))
public final class TableTransformHook implements Hook {
  private final Log log;
  private final DatabaseKeyspace keyspace;
  private final WorkerProxyClient proxyClient;

  @EventHook
  private void tableTransform(TableTransformEvent event) {
    var tableOptional = keyspace.findTableByClass(event.tableClass());
    if (tableOptional.isEmpty()) {
      return;
    }
    var nextState = switch (tableOptional.get().transformationState()) {
      case INACTIVE -> DatabaseTransformationState.FILL_TEMPORARY;
      case FILL_TEMPORARY -> DatabaseTransformationState.USE_TEMPORARY;
      case USE_TEMPORARY -> DatabaseTransformationState.FILL_NEW;
      case FILL_NEW -> DatabaseTransformationState.USE_NEW;
      default -> DatabaseTransformationState.FAILURE;
    };
    if (nextState.isFailure()) {
      log.severe("This pod was determined for the transformation of table " +
        event.tableClass() + ". Unfortunately, the transformation could not " +
        "be started / continued.");
      return;
    }
    proxyClient.sendPacket(new PacketOutgoingTableStateRequest(
      event.tableClass(), nextState));
    log.info("This pod was determined for the transformation of table " +
      event.tableClass() + ". The transformation is now initiated.");
  }
}