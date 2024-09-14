package com.dulno.core.bundle;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum BundleClass {
  NONE(-1),
  BEGINNER(1),
  ADVANCED(2),
  EXPERT(3);

  private final int weight;

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
