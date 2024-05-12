package net.taskwolf.core.command;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.taskwolf.core.log.Log;

@Accessors(fluent = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class Command {
  @Getter(AccessLevel.PROTECTED)
  private final Log log;
  @Getter
  private final String name;
  @Getter
  private final String[] aliases;
  @Getter
  private final String[] arguments;

  /**
   * Is called when a command is executed
   * @param arguments The entered arguments
   * @return Success
   * @throws Exception
   */
  public abstract boolean execute(String[] arguments) throws Exception;
}
