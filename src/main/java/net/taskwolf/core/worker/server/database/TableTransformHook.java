package net.taskwolf.core.worker.server.database;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import net.taskwolf.core.database.transformation.DatabaseTransformationState;
import net.taskwolf.core.event.EventHook;
import net.taskwolf.core.event.Hook;
import net.taskwolf.core.log.Log;
import net.taskwolf.core.worker.client.WorkerProxyClient;
import net.taskwolf.core.worker.event.database.TableTransformEvent;
import net.taskwolf.core.worker.packet.outgoing.database.PacketOutgoingTableStateRequest;

@Singleton
@RequiredArgsConstructor(access = AccessLevel.PRIVATE, onConstructor = @__({@Inject}))
public final class TableTransformHook implements Hook {
  private final Log log;
  private final WorkerProxyClient proxyClient;

  @EventHook
  private void tableTransform(TableTransformEvent event) {
    proxyClient.sendPacket(new PacketOutgoingTableStateRequest(event.tableClass(),
      DatabaseTransformationState.FILL_TEMPORARY));
    log.info("This pod was determined for the transformation of table " +
      event.tableClass() + ". The transformation is now initiated.");
  }
}