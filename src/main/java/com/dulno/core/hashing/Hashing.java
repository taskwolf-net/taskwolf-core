package com.dulno.core.hashing;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;

@Singleton
@RequiredArgsConstructor(access = AccessLevel.PRIVATE, onConstructor = @__({@Inject}))
public final class Hashing {
  private final Argon2PasswordEncoder encoder =
    new Argon2PasswordEncoder(16, 32, 1, 65536, 3);

  public String hash(String input) {
    return encoder.encode(input);
  }

  public boolean matches(String input, String hash) {
    return encoder.matches(input, hash);
  }
}
