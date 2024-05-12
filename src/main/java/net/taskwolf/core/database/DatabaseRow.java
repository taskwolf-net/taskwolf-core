package net.taskwolf.core.database;

import com.datastax.oss.driver.api.core.cql.Row;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
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

  /**
   * Creates a string that contains all cells and that can be used by cassandra
   * @return The value compilation
   */
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

  /**
   * Combines two rows into one common row
   * @param other The other row
   * @return The common row
   */
  public DatabaseRow concat(DatabaseRow other) {
    var result = Arrays.copyOf(cells, this.cellNumber() + other.cellNumber());
    System.arraycopy(other.raw(), 0, result, this.cellNumber(), other.cellNumber());
    return DatabaseRow.create(result);
  }

  /**
   * Used to search fo single cell in row
   * @param index The index of the target cell
   * @return The searched cell of the row
   */
  public DatabaseCell findCell(int index) {
    return cells[index];
  }

  /**
   * Calculates the number of stored cells in the row
   * @return The number of cells
   */
  public int cellNumber() {
    return cells.length;
  }

  /**
   * Is used to get the raw cell array of the database row
   * @return The raw cell array
   */
  public DatabaseCell[] raw() {
    return Arrays.copyOf(cells, cells.length);
  }
}
