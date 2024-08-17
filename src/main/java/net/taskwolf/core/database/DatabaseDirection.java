package net.taskwolf.core.database;

public enum DatabaseDirection {
  FORWARD,
  BACKWARD;

  public boolean isForward() {
    return this == FORWARD;
  }

  public boolean isBackward() {
    return this == BACKWARD;
  }
}
