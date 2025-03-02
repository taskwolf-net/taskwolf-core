package com.dulno.core.environment;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class DulnoEnvironment {
  public static DulnoEnvironment create() {
    var environment = new DulnoEnvironment();
    environment.initialize();
    return environment;
  }

  private enum Type {
    PRODUCTIVE,
    STAGING,
    LOCAL
  }

  private Type type;

  public void initialize() {
    try {
      type = Type.valueOf(System.getenv("DULNO_ENVIRONMENT"));
    } catch (Exception exception) {
      type = Type.LOCAL;
    }
  }

  public boolean isProductive() {
    return type == Type.PRODUCTIVE;
  }

  public boolean isStaging() {
    return type == Type.STAGING;
  }

  public boolean isLocal() {
    return type == Type.LOCAL;
  }

  public String domain() {
    if (isProductive()) {
      return "dulno.com";
    }
    return "dulno.dev";
  }

  public String publicEndpoint() {
    if (isProductive()) {
      return "api.dulno.com";
    }
    return "pub.dulno.dev";
  }
}
