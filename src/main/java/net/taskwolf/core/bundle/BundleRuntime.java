package net.taskwolf.core.bundle;

public enum BundleRuntime {
  MONTHLY,
  YEARLY;

  public boolean isMonthly() {
    return this == MONTHLY;
  }

  public boolean isYearly() {
    return this == YEARLY;
  }
}
