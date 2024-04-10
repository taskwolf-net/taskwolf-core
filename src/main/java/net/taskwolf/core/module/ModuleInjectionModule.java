package net.taskwolf.core.module;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.taskwolf.core.distribution.Distribution;
import net.taskwolf.core.log.Log;

@RequiredArgsConstructor(staticName = "create")
public final class ModuleInjectionModule extends AbstractModule {
  @Provides
  @Singleton
  ModuleLoader provideModuleLoader(
    Log log, Distribution distribution, Injector injector
  ) {
    return ModuleLoader.create(log, System.getProperty("user.dir") +
      "/modules/", distribution, injector);
  }
}
