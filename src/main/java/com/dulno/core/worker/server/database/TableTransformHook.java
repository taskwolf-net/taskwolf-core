package com.dulno.core.worker.server.database;

import com.dulno.core.worker.client.WorkerProxyClient;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import com.dulno.core.database.DatabaseKeyspace;
import com.dulno.core.database.transformation.DatabaseTransformationState;
import com.dulno.core.event.EventHook;
import com.dulno.core.event.Hook;
import com.dulno.core.log.Log;
import com.dulno.core.worker.event.database.TableTransformEvent;
import com.dulno.core.worker.packet.outgoing.database.PacketOutgoingTableStateRequest;

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