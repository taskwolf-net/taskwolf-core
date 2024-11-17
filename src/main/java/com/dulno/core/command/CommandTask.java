package com.dulno.core.command;

import com.dulno.core.error.ErrorRepository;
import com.dulno.core.log.Log;
import lombok.RequiredArgsConstructor;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.logging.Level;

@RequiredArgsConstructor(staticName = "create")
public final class CommandTask {
  private final Log log;
  private final ErrorRepository errorRepository;
  private final CommandRegistry commandRegistry;

  /**
   * Stats the command task (begins to monitor command line inputs)
   */
  public void start() {
    var reader = new BufferedReader(new InputStreamReader(System.in));
    try {
      monitorInput(reader);
    } catch (Exception exception) {
      errorRepository.processError(exception);
    }
  }

  private void monitorInput(BufferedReader reader) throws Exception {
    var line = "";
    while((line = reader.readLine()) != null) {
      if(line.isEmpty()) {
        printNewLine();
        continue;
      }
      superviseInput(line);
    }
  }

  private void superviseInput(String line) {
    var input = line.split(" ");
    var commandName = input[0];
    commandRegistry.find(commandName).ifPresentOrElse(command ->
      executeCommand(command, line, input), () -> printCommandNotFound(commandName));
  }

  private void executeCommand(Command command, String line, String[] input) {
    var length = input.length - 1;
    var arguments = new String[length];
    System.arraycopy(input,1, arguments,0, length);
    try {
      log.fileLog(Level.INFO, "Entered command '" + line + "'");
      if (!command.execute(arguments)) {
        printOutSyntax(command);
      }
    } catch (Exception exception) {
      errorRepository.processError(exception);
    }
  }

  private void printOutSyntax(Command command) {
    var arguments = command.arguments();
    var syntax = new StringBuilder("Syntax: " + command.name() + " ");
    for (var i = 0; i < arguments.length; i++) {
      syntax.append(arguments[i]).append(i != arguments.length - 1 ? " / " : " ");
    }
    log.info(syntax.toString());
  }

  private void printCommandNotFound(String command) {
    log.info("Command '" + command + "' not found");
  }

  private void printNewLine() {
    System.out.print(" > ");
  }
}
