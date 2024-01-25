package net.taskwolf.core.whitelist;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(staticName = "create")
public final class WhitelistInjectionModule extends AbstractModule {
  @Provides
  @Singleton
  WhitelistConfiguration provideWhitelistConfiguration() throws Exception {
    return WhitelistConfiguration.createAndLoad();
  }
}
