package net.taskwolf.core.access;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Key;

@RequiredArgsConstructor(staticName = "create")
public final class AccessInjectionModule extends AbstractModule {
  @Provides
  @Singleton
  Key provideSecretKey() throws Exception {
    return new SecretKeySpec(VerificationConfiguration.createAndLoad()
      .verificationSecret().getBytes(StandardCharsets.UTF_8),
      SignatureAlgorithm.HS256.getJcaName());
  }
}
