package de.flexpedite.core.database;

import com.datastax.oss.driver.api.core.cql.Row;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor(staticName = "create")
public final class DatabaseRow {
  public static List<DatabaseRow> multiple(Iterable<Row> iterable, int columnsLength) {
    List<DatabaseRow> rows = Lists.newArrayList();
    for (Row row : iterable) {
      rows.add(of(row, columnsLength));
    }
    return rows;
  }

  public static DatabaseRow of(Row row, int columnsLength) {
    var cells = new DatabaseCell[columnsLength];
    for (var i = 0; i < columnsLength; i++) {
      cells[i] = DatabaseCell.create(row.getObject(i));
    }
    return create(cells);
  }

  public static DatabaseRow of(Object... values) {
    var cells = new DatabaseCell[values.length];
    for (var i = 0; i < values.length; i++) {
      cells[i] = DatabaseCell.create(values[i]);
    }
    return create(cells);
  }

  private final DatabaseCell[] cells;

  public String valuesCompilation() {
    var compilation = new StringBuilder();
    for (var i = 0; i < cells.length; i++) {
      compilation.append(cells[i].databaseValue());
      if (i < cells.length - 1) {
        compilation.append(", ");
      }
    }
    return compilation.toString();
  }

  public DatabaseCell findCell(int index) {
    return cells[index];
  }
}
