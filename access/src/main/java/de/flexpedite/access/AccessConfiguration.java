package de.flexpedite.access;

import com.google.inject.Guice;
import com.google.inject.Inject;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.Key;

@Configuration
public class AccessConfiguration {
  @Inject
  private Key secretKey;

  @Bean
  Key provideSecretKey() {
    return secretKey;
  }

  @PostConstruct
  private void createModuleInjector() {
    var injector = Guice.createInjector(AccessModule.create());
    injector.injectMembers(this);
  }
}
