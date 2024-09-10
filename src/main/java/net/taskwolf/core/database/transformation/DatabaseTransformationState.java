package net.taskwolf.core.database.transformation;

public enum DatabaseTransformationState {
  INACTIVE,
  FILL_TEMPORARY,
  USE_TEMPORARY,
  FILL_NEW,
  USE_NEW,
  FAILURE;

  public boolean isInactive() {
    return this == INACTIVE;
  }

  public boolean isFillTemporary() {
    return this == FILL_TEMPORARY;
  }

  public boolean isUseTemporary() {
    return this == USE_TEMPORARY;
  }

  public boolean isFillNew() {
    return this == FILL_NEW;
  }

  public boolean isUseNew() {
    return this == USE_NEW;
  }

  public boolean isFailure() {
    return this == FAILURE;
  }
}
