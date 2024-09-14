package com.dulno.core.module;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ModuleDescription {
  /**
   * The internal name of the module
   * @return The module name
   */
  String name();

  /**
   * The current version of the module
   * @return The module version
   */
  String version();

  /**
   * The load priority of the module (Determines the loading sequence)
   * @return The module load priority
   */
  ModuleLoadPriority priority();
}