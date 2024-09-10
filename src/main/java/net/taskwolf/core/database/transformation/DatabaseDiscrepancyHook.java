package net.taskwolf.core.database.transformation;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import net.taskwolf.core.application.CoreApplicationPostRunEvent;
import net.taskwolf.core.application.CoreApplicationPreRunEvent;
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
  private void preApplicationRun(CoreApplicationPreRunEvent event) {
    new Thread(this::checkTablesDiscrepancy).start();
  }

  private void checkTablesDiscrepancy() {
    for (var table : keyspace.tables()) {
      table.checkTableDiscrepancy().join();
    }
  }

  @EventHook
  private void applicationRun(CoreApplicationPostRunEvent event) {
    new Thread(this::sendDiscrepancyNotices).start();
  }

  private void sendDiscrepancyNotices() {
    try {
      Thread.sleep(10000);
    } catch (Exception exception) {
      exception.printStackTrace();
    }
    for (var table : keyspace.tables()) {
      senDiscrepancyNotice(table);
    }
  }

  private void senDiscrepancyNotice(DatabaseTable table) {
    if (table.temporaryTable() == null) {
      return;
    }
    proxyClient.sendPacket(new PacketOutgoingTableDiscrepancy(
      table.getClass().getCanonicalName()));
    log.info("A discrepancy was found in table " + table.getClass().getCanonicalName() +
      ". The transformation is being prepared.");
  }
}
