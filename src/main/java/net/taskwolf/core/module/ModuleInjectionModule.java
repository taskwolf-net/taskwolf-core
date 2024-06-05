package net.taskwolf.core.module;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import lombok.RequiredArgsConstructor;
import net.taskwolf.core.locale.Locale;
import net.taskwolf.core.log.Log;
import net.taskwolf.core.worker.WorkerDistribution;

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
