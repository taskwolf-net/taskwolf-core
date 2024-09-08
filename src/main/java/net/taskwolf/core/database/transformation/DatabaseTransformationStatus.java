package net.taskwolf.core.database.transformation;

public enum DatabaseTransformationStatus {
  RUNNING,
  IDLE;

  public boolean isRunning() {
    return this == RUNNING;
  }

  public boolean isIdle() {
    return this == IDLE;
  }
}
