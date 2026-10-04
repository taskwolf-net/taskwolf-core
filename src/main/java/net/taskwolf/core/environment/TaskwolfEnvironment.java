package net.taskwolf.core.environment;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class TaskwolfEnvironment {
  public static TaskwolfEnvironment create() {
    var environment = new TaskwolfEnvironment();
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
      type = Type.valueOf(System.getenv("TASKWOLF_ENVIRONMENT"));
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
      return "taskwolf.net";
    }
    return "taskwolf.dev";
  }

  public String publicEndpoint() {
    if (isProductive()) {
      return "api.taskwolf.net";
    }
    return "pub.taskwolf.dev";
  }
}
