package net.taskwolf.core.intro;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(staticName = "create")
public final class IntroInjectionModule extends AbstractModule {
  @Provides
  @Singleton
  Intro provideIntro() {
    return Intro.create("1.0.0");
  }
}
