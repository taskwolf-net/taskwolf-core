package com.dulno.core.command;

import com.google.common.collect.Lists;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Singleton
@RequiredArgsConstructor(staticName = "create")
public final class CommandRegistry {
  private final List<Command> commands = Lists.newArrayList();

  /**
   * Registers a new command
   * @param command The command that is to be registered
   */
  public void register(Command command) {
    commands.add(command);
  }

  /**
   * Unregisters a command
   * @param command The command that is to be unregistered
   */
  public void unregister(Command command) {
    commands.remove(command);
  }

  /**
   * Is used to find command by input (compares command name / aliases)
   * @param input The command name or one command alias
   * @return The command if it could be found
   */
  public Optional<Command> find(String input) {
    return commands.stream()
      .filter(command -> command.name().equalsIgnoreCase(input) ||
        Arrays.stream(command.aliases()).anyMatch(alias -> alias.equalsIgnoreCase(input)))
      .findFirst();
  }
}
