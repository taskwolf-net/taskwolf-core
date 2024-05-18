package net.taskwolf.core.worker;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.taskwolf.core.event.EventExecutor;
import net.taskwolf.core.packet.PacketEventRepository;
import net.taskwolf.core.packet.PacketRegistry;
import net.taskwolf.core.worker.client.WorkerProxyClient;

@RequiredArgsConstructor(staticName = "create")
public final class WorkerInjectionModule extends AbstractModule {
  @Provides
  @Singleton
  WorkerConfiguration provideDistributionConfiguration() throws Exception {
    return WorkerConfiguration.createAndLoad();
  }

  @Provides
  @Singleton
  WorkerProxyClient provideWorkerProxyClient(
    WorkerConfiguration configuration, PacketRegistry packetRegistry,
    EventExecutor eventExecutor, PacketEventRepository packetEventRepository
  ) {
    return WorkerProxyClient.create(configuration, packetRegistry,
      eventExecutor, packetEventRepository, configuration.proxyHostname(),
      configuration.proxyDistributionPort());
  }
}
