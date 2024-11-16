package com.dulno.core.error;

public enum DulnoErrorState {
  UNSOLVED,
  FIXED;

  public boolean isUnsolved() {
    return this == UNSOLVED;
  }

  public boolean isFixed() {
    return this == FIXED;
  }
}
