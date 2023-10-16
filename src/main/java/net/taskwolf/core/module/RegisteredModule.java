package net.taskwolf.core.module;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import java.io.File;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(staticName = "create")
public final class RegisteredModule {
  private final Module module;
  private final String name;
  private final String version;
  private final ModuleLoadPriority priority;
  private final File file;
}
