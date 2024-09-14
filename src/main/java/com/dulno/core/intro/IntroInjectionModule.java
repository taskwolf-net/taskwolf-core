package com.dulno.core.intro;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import com.dulno.core.log.Log;

@RequiredArgsConstructor(staticName = "create")
public final class IntroInjectionModule extends AbstractModule {
  @Provides
  @Singleton
  Intro provideIntro(Log log) {
    return Intro.create(log, "1.0.0");
  }
}
