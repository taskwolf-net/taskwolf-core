package net.taskwolf.core.command;

import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor(staticName = "create")
public final class CommandHistory {
  private final List<String> history = Lists.newArrayList();
  private int currentCommand = 0;

  public void storeCommand(String command) {
    history.add(command);
  }

  public String nextCommand() {
    if (currentCommand + 1 >= history.size()) {
      currentCommand = history.size();
      return "";
    }
    currentCommand++;
    return history.get(currentCommand);
  }

  public String previousCommand() {
    if (currentCommand -1 < 0) {
      currentCommand = -1;
      return "";
    }
    currentCommand--;
    return history.get(currentCommand);
  }

  public void resetCurrentCommand() {
    currentCommand = history.size();
  }
}
