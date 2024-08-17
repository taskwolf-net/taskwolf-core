package net.taskwolf.core.database;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(staticName = "create")
public final class DatabasePage {
  private final List<DatabaseRow> rows;
  private final String pageState;
}
