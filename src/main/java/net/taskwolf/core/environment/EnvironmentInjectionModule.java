package net.taskwolf.core.environment;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(staticName = "create")
public final class EnvironmentInjectionModule extends AbstractModule {
  @Provides
  @Singleton
  TaskwolfEnvironment provideTaskwolfEnvironment() {
    return TaskwolfEnvironment.create();
  }
}

