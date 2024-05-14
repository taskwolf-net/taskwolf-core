package net.taskwolf.core.database;

import com.google.common.collect.Lists;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.UUID;

final class DatabaseCellTest {
  @Test
  void testDatabaseCell() {
    var stringCell = DatabaseCell.create("Test");
    Assertions.assertEquals(stringCell.stringValue(), "Test");
    Assertions.assertEquals(stringCell.databaseValue(), "'Test'");
    var intCell = DatabaseCell.create(10);
    Assertions.assertEquals(intCell.integerValue(), 10);
    Assertions.assertEquals(intCell.databaseValue(), "10");
    var uuid = UUID.randomUUID();
    var uuidCell = DatabaseCell.create(uuid);
    Assertions.assertEquals(uuidCell.uuidValue(), uuid);
    Assertions.assertEquals(uuidCell.databaseValue(), uuid.toString());
    var list = Lists.newArrayList(1, 2, 3);
    var listCell = DatabaseCell.create(list);
    Assertions.assertEquals(listCell.listValue(), list);
    Assertions.assertEquals(listCell.databaseValue(), "[1, 2, 3]");
  }
}
