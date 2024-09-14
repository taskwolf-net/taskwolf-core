package com.dulno.core.locale;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(staticName = "create")
public final class LocaleInjectionModule extends AbstractModule {
  @Provides
  @Singleton
  @Named("englishLocale")
  Locale provideEnglishLocale() throws Exception {
    return Locale.createAndLoad("core", "en");
  }

  @Provides
  @Singleton
  @Named("germanLocale")
  Locale provideGermanLocale() throws Exception {
    return Locale.createAndLoad("core", "de");
  }
}
