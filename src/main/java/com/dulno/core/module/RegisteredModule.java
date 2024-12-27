package com.dulno.core.module;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(staticName = "create")
public final class RegisteredModule {
  private final Module module;
  private final String name;
  private final String version;
  private final ModuleLoadPriority priority;
}
