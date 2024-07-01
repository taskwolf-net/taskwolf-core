package net.taskwolf.core.bundle;

public enum BundleClass {
  NONE,
  BEGINNER,
  ADVANCED,
  EXPERT;

  public boolean isNone() {
    return this == NONE;
  }

  public boolean isBeginner() {
    return this == BEGINNER;
  }

  public boolean isAdvanced() {
    return this == ADVANCED;
  }

  public boolean isExpert() {
    return this == EXPERT;
  }
}
