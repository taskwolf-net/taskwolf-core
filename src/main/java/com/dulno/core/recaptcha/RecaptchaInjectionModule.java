package com.dulno.core.recaptcha;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(staticName = "create")
public final class RecaptchaInjectionModule extends AbstractModule {
  @Provides
  @Singleton
  RecaptchaConfiguration provideRecaptchaConfiguration() throws Exception {
    return RecaptchaConfiguration.createAndLoad();
  }
}
