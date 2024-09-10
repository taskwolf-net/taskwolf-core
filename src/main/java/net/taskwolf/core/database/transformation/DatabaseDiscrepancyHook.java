package net.taskwolf.core.database.transformation;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import net.taskwolf.core.application.CoreApplicationRunEvent;
import net.taskwolf.core.database.DatabaseKeyspace;
import net.taskwolf.core.database.DatabaseTable;
import net.taskwolf.core.event.EventHook;
import net.taskwolf.core.event.Hook;
import net.taskwolf.core.log.Log;
import net.taskwolf.core.worker.client.WorkerProxyClient;
import net.taskwolf.core.worker.packet.outgoing.database.PacketOutgoingTableDiscrepancy;

@Singleton
@RequiredArgsConstructor(access = AccessLevel.PRIVATE, onConstructor = @__({@Inject}))
public final class DatabaseDiscrepancyHook implements Hook {
  private final Log log;
  private final DatabaseKeyspace keyspace;
  private final WorkerProxyClient proxyClient;

  @EventHook
  private void applicationRun(CoreApplicationRunEvent event) {
    new Thread(this::checkTablesDiscrepancy).start();
  }

  private void checkTablesDiscrepancy() {
    try {
      Thread.sleep(10000);
    } catch (Exception exception) {
      exception.printStackTrace();
    }
    for (var table : keyspace.tables()) {
      table.checkTableDiscrepancy().thenAccept(discrepancy ->
        checkTableDiscrepancy(table, discrepancy));
    }
  }

  private void checkTableDiscrepancy(DatabaseTable table, boolean discrepancy) {
    if (!discrepancy) {
      return;
    }
    proxyClient.sendPacket(new PacketOutgoingTableDiscrepancy(
      table.getClass().getCanonicalName()));
    log.info("A discrepancy was found in table " + table.getClass().getCanonicalName() +
      ". The transformation is being prepared.");
  }
}
