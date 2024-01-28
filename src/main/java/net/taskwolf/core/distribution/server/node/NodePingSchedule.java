package net.taskwolf.core.distribution.server.node;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import net.taskwolf.core.distribution.client.DistributionClient;
import net.taskwolf.core.distribution.client.DistributionClientRegistry;
import net.taskwolf.core.distribution.client.packet.node.PacketOutgoingPing;
import net.taskwolf.core.log.Log;

import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Singleton
@RequiredArgsConstructor(access = AccessLevel.PRIVATE, onConstructor = @__({@Inject}))
public final class NodePingSchedule {
  private final NodePingCache pingCache;
  private final DistributionClientRegistry distributionClientRegistry;
  private final Log log;
  private final Random random = new Random();
  private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
  private ScheduledFuture<?> scheduler;

  private static final int PING_DELAY = 5;
  private static final TimeUnit PING_TIME_UNIT = TimeUnit.MINUTES;

  public void start() {
    scheduler = executorService.scheduleAtFixedRate(this::schedule,
      PING_DELAY, PING_DELAY, PING_TIME_UNIT);
  }

  private void schedule() {
    processNonRespondingClients();
    sendPings();
  }

  private void processNonRespondingClients() {
    var nonRespondingClient = pingCache.findPendingPingClients();
    for (var client : nonRespondingClient) {
      client.disconnect();
      log.severe("The node " + client.node().information() + " has not " +
        "responded to a ping. The connection to the node was therefore " +
        "interrupted. Manual intervention is required to resolve the problem");
    }
    pingCache.clear();
  }

  private void sendPings() {
    for (var client : distributionClientRegistry.findAllClients()) {
      sendPing(client);
    }
  }

  private void sendPing(DistributionClient client) {
    var value = random.nextInt();
    pingCache.insertNodePing(client, value);
    client.sendPacket(new PacketOutgoingPing(value));
  }

  public void stop() {
    scheduler.cancel(false);
  }
}
