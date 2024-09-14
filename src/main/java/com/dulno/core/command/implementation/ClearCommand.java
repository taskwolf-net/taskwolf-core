package com.dulno.core.command.implementation;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.dulno.core.command.Command;
import com.dulno.core.intro.Intro;
import com.dulno.core.log.Log;

@Singleton
public final class ClearCommand extends Command {
  private final Intro intro;

  @Inject
  private ClearCommand(Log log, Intro intro) {
    super(log, "clear", new String[0], new String[0]);
    this.intro = intro;
  }

  @Override
  public boolean execute(String[] arguments) {
    System.out.print("\033c");
    log().resetCurrentLogLine();
    intro.print();
    log().info("The screen has been cleared");
    return true;
  }
}
