package com.dulno.core.error;

import com.dulno.core.database.DatabaseRow;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.UUID;

@Getter
@Accessors(fluent = true)
@AllArgsConstructor(staticName = "create")
public final class DulnoError {
  public static DulnoError of(
    UUID id, String origin, String pod, String podControllerName,
    String podControllerType, String node, Throwable throwable
  ) {
    var stringWriter = new StringWriter();
    var printWriter = new PrintWriter(stringWriter);
    throwable.printStackTrace(printWriter);
    return create(id, throwable.toString(), stringWriter.toString(),
      origin, pod, podControllerName, podControllerType, node,
      System.currentTimeMillis(), DulnoErrorState.UNSOLVED);
  }

  public static DulnoError of(DatabaseRow row) {
    return create(row.findCell(0).uuidValue(), row.findCell(1).stringValue(),
      row.findCell(2).stringValue(), row.findCell(3).stringValue(),
      row.findCell(4).stringValue(), row.findCell(5).stringValue(),
      row.findCell(6).stringValue(), row.findCell(7).stringValue(),
      row.findCell(8).longValue(),
      DulnoErrorState.valueOf(row.findCell(9).stringValue()));
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
  private DulnoErrorState state;

  public void updateState(DulnoErrorState newState) {
    state = newState;
  }
}
