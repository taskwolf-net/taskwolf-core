package net.taskwolf.core.access;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Key;

@RequiredArgsConstructor(staticName = "create")
public final class AccessInjectionModule extends AbstractModule {
  @Provides
  @Singleton
  VerificationConfiguration provideVerificationConfiguration() throws Exception {
    return VerificationConfiguration.createAndLoad();
  }

  @Provides
  @Singleton
  @Named("homeKey")
  Key provideHomeKey(VerificationConfiguration configuration) {
    return new SecretKeySpec(configuration.homeSecret()
      .getBytes(StandardCharsets.UTF_8), SignatureAlgorithm.HS256.getJcaName());
  }

  @Provides
  @Singleton
  @Named("productKey")
  Key provideProductKey(VerificationConfiguration configuration) {
    return new SecretKeySpec(configuration.productSecret()
      .getBytes(StandardCharsets.UTF_8), SignatureAlgorithm.HS256.getJcaName());
  }
}
