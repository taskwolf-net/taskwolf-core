package net.taskwolf.core.bundle;

public enum BundleClass {
  BEGINNER,
  ADVANCED,
  EXPERT;

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
