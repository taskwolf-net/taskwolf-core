package net.taskwolf.core.tutorial;

import net.taskwolf.core.database.DatabaseRow;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.UUID;

@Getter
@Accessors(fluent = true)
@AllArgsConstructor(staticName = "create")
public final class Tutorial {
  public static Tutorial of(DatabaseRow row) {
    return create(row.findCell(0).uuidValue(), row.findCell(1).integerValue(),
      row.findCell(2).integerValue());
  }

  private final UUID user;
  private int level;
  private int step;

  public void updateLevel(int newLevel) {
    level = newLevel;
  }

  public void updateStep(int newStep) {
    step = newStep;
  }
}
