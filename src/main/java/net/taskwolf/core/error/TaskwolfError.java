package net.taskwolf.core.error;

import net.taskwolf.core.database.DatabaseRow;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.UUID;

@Getter
@Accessors(fluent = true)
@AllArgsConstructor(staticName = "create")
public final class TaskwolfError {
  public static TaskwolfError of(
    UUID id, String origin, String pod, String podControllerName,
    String podControllerType, String node, Throwable throwable
  ) {
    var stringWriter = new StringWriter();
    var printWriter = new PrintWriter(stringWriter);
    throwable.printStackTrace(printWriter);
    return create(id, throwable.toString(), stringWriter.toString(),
      origin, pod, podControllerName, podControllerType, node,
      System.currentTimeMillis(), TaskwolfErrorState.UNSOLVED);
  }

  public static TaskwolfError of(DatabaseRow row) {
    return create(row.findCell(0).uuidValue(), row.findCell(1).stringValue(),
      row.findCell(2).stringValue(), row.findCell(3).stringValue(),
      row.findCell(4).stringValue(), row.findCell(5).stringValue(),
      row.findCell(6).stringValue(), row.findCell(7).stringValue(),
      row.findCell(8).longValue(),
      TaskwolfErrorState.valueOf(row.findCell(9).stringValue()));
  }

  private final UUID id;
  private final String title;
  private final String trace;
  private final String origin;
  private final String pod;
  private final String podControllerName;
  private final String podControllerType;
  private final String node;
  private final long time;
  private TaskwolfErrorState state;

  public void updateState(TaskwolfErrorState newState) {
    state = newState;
  }
}
