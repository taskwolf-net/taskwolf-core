package com.dulno.core.module;

import com.dulno.core.locale.Locale;
import com.dulno.core.log.Log;
import com.dulno.core.worker.WorkerDistribution;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(staticName = "create")
public final class ModuleInjectionModule extends AbstractModule {
  @Provides
  @Singleton
  ModuleLoader provideModuleLoader(
    Log log, WorkerDistribution distribution,
    @Named("englishLocale") Locale englishLocale,
    @Named("germanLocale") Locale germanLocale, Injector injector
  ) {
    return ModuleLoader.create(log, System.getProperty("user.dir") +
      "/modules/", distribution, englishLocale, germanLocale, injector);
  }
}
