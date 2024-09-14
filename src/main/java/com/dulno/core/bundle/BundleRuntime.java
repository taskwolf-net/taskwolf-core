package com.dulno.core.bundle;

public enum BundleRuntime {
  WEEKLY,
  MONTHLY,
  YEARLY;

  public boolean isWeekly() {
    return this == WEEKLY;
  }

  public boolean isMonthly() {
    return this == MONTHLY;
  }

  public boolean isYearly() {
    return this == YEARLY;
  }
}
