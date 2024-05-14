package net.taskwolf.core.database;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

final class DatabaseRowTest {
  @Test
  void testDatabaseRow() {
    var row = DatabaseRow.create(new DatabaseCell[] {DatabaseCell.create("Test"),
      DatabaseCell.create(1), DatabaseCell.create(List.of("Test"))});
    Assertions.assertEquals(row.cellNumber(), 3);
    Assertions.assertEquals(row.findCell(0).stringValue(), "Test");
    Assertions.assertEquals(row.findCell(1).integerValue(), 1);
    Assertions.assertEquals(row.findCell(2).listValue(), List.of("Test"));
    Assertions.assertEquals(row.valuesCompilation(), "'Test', 1, ['Test']");
    row = row.concat(DatabaseRow.of(UUID.randomUUID()));
    Assertions.assertEquals(row.cellNumber(), 4);
  }
}
