package com.dulno.core.bundle;

public enum BundleRuntime {
  MONTHLY,
  YEARLY,
  INFINITE,
  UNBOUND;

  public boolean isMonthly() {
    return this == MONTHLY;
  }

  public boolean isYearly() {
    return this == YEARLY;
  }

  public boolean isInfinite() {
    return this == INFINITE;
  }

  public boolean isUnbound() {
    return this == UNBOUND;
  }
}
