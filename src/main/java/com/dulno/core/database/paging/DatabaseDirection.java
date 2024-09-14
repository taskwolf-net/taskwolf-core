package com.dulno.core.database.paging;

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
