package net.taskwolf.core.command;

import lombok.RequiredArgsConstructor;
import net.taskwolf.core.log.Log;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.util.logging.Level;

@RequiredArgsConstructor(staticName = "create")
public final class CommandTask {
  private final Log log;
  private final CommandRegistry commandRegistry;
  private final CommandHistory commandHistory = CommandHistory.create();
  private Terminal terminal;
  private String currentInput = "";
  private int horizontalCursorPosition = 0;

  public void start() {
    try {
      terminal = TerminalBuilder.builder().jna(true).system(true).build();
      terminal.enterRawMode();
      monitorInput();
    } catch (Exception exception) {
      exception.printStackTrace();
    }
  }

  private void monitorInput() throws Exception {
    var reader = terminal.reader();
    while (true) {
      var input = reader.read();
      interpretInput(input);
    }
  }

  private static final int ENTER_KEY_CODE = 13;
  private static final int BACKSPACE_KEY_CODE = 127;

  private void interpretInput(int code) {
    if (code == ENTER_KEY_CODE) {
      submitInput();
      return;
    }
    if (code == BACKSPACE_KEY_CODE) {
      submitBackspace();
      return;
    }
    storeInput(code);
    checkEscapeCodeInput();
  }

  private void storeInput(int code) {
    var input = (char) code;
    currentInput = currentInput.substring(0, horizontalCursorPosition) +
      input + currentInput.substring(horizontalCursorPosition);
    resetLine();
    horizontalCursorPosition += 1;
    repositionCursor();
  }

  private void checkEscapeCodeInput() {
    if (inputContainsEscapeCode('A') || inputContainsEscapeCode('B')) {
      processVerticalInput();
    } else if (inputContainsEscapeCode('C') || inputContainsEscapeCode('D')) {
      processHorizontalInput();
    }
  }

  private void processVerticalInput() {
    var isUp = inputContainsEscapeCode('A');
    currentInput = isUp ? commandHistory.previousCommand() :
      commandHistory.nextCommand();
    horizontalCursorPosition = currentInput.length();
    resetLineAndRepositionCursor();
  }

  private void processHorizontalInput() {
    var isLeft = inputContainsEscapeCode('D');
    currentInput = currentInput.replace(isLeft ? formatEscapeCode('D') :
      formatEscapeCode('C'), "");
    horizontalCursorPosition -= 3;
    if (!(isLeft && horizontalCursorPosition - 1 < 0 ||
      !isLeft && horizontalCursorPosition + 1 > currentInput.length())
    ) {
      horizontalCursorPosition += isLeft ? -1 : 1;
    }
    resetLineAndRepositionCursor();
  }

  private boolean inputContainsEscapeCode(char value) {
    return currentInput.contains(formatEscapeCode(value));
  }

  private static final char ESCAPE_CODE = (char) 27;

  private String formatEscapeCode(char value) {
    return ESCAPE_CODE + "[" + value;
  }

  private void submitBackspace() {
    if (currentInput.isEmpty() || horizontalCursorPosition <= 0) {
      return;
    }
    currentInput = currentInput.substring(0, horizontalCursorPosition - 1) +
      currentInput.substring(horizontalCursorPosition);
    resetLine();
    horizontalCursorPosition -= 1;
    repositionCursor();
  }

  private void submitInput() {
    if (currentInput.isEmpty()) {
      printNewLine();
      return;
    }
    var input = currentInput.split(" ");;
    var commandName = input[0];
    commandRegistry.find(commandName).ifPresentOrElse(command ->
        executeCommand(command, currentInput, input),
      () -> printCommandNotFound(commandName));
    commandHistory.storeCommand(currentInput);
    commandHistory.resetCurrentCommand();
    currentInput = "";
    horizontalCursorPosition = 0;
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
      exception.printStackTrace();
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
    System.out.println();
    log.increaseCurrentLogLine();
    resetLine();
  }

  private void resetLineAndRepositionCursor() {
    resetLine();
    repositionCursor();
  }

  private void resetLine() {
    System.out.print("\r\033[2K > " + currentInput);
  }

  private void repositionCursor() {
    System.out.print(String.format("%c[%d;%df", 0x1B, log.currentLogLine() + 1,
      horizontalCursorPosition + 4));
  }
}
