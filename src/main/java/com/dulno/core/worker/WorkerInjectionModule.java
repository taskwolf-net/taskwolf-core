package com.dulno.core.worker;

import com.dulno.core.event.EventExecutor;
import com.dulno.core.packet.PacketEventRepository;
import com.dulno.core.packet.PacketRegistry;
import com.dulno.core.worker.client.WorkerProxyClient;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;

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
