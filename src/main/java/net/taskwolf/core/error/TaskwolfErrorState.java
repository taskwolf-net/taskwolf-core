package net.taskwolf.core.error;

public enum TaskwolfErrorState {
  UNSOLVED,
  FIXED;

  public boolean isUnsolved() {
    return this == UNSOLVED;
  }

  public boolean isFixed() {
    return this == FIXED;
  }
}
