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
  public static DulnoError of(UUID id, Throwable throwable) {
    var stringWriter = new StringWriter();
    var printWriter = new PrintWriter(stringWriter);
    throwable.printStackTrace(printWriter);
    return create(id, throwable.getMessage(), stringWriter.toString(),
      System.currentTimeMillis(), DulnoErrorState.UNSOLVED);
  }

  public static DulnoError of(DatabaseRow row) {
    return create(row.findCell(0).uuidValue(), row.findCell(1).stringValue(),
      row.findCell(2).stringValue(), row.findCell(3).longValue(),
      DulnoErrorState.valueOf(row.findCell(4).stringValue()));
  }

  private final UUID id;
  private final String title;
  private final String trace;
  private final long time;
  private DulnoErrorState state;

  public void updateState(DulnoErrorState newState) {
    state = newState;
  }
}
