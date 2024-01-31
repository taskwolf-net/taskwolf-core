package net.taskwolf.core.distribution;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(staticName = "create")
public final class DistributionInjectionModule extends AbstractModule {
  @Provides
  @Singleton
  DistributionConfiguration provideDistributionConfiguration() throws Exception {
    return DistributionConfiguration.createAndLoad();
  }
}
