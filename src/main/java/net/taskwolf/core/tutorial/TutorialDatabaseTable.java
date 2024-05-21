package net.taskwolf.core.tutorial;

import com.google.common.collect.Lists;
import net.taskwolf.core.database.*;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class TutorialDatabaseTable extends DatabaseTable {
  private static final String TABLE_NAME = "tutorial";

  public static TutorialDatabaseTable create(
    DatabaseConnection connection, DatabaseKeyspace keyspace
  ) {
    var columns = Lists.<DatabaseColumn>newArrayList();
    columns.add(DatabaseColumn.create("user", DatabaseDataType.UUID,
      DatabaseColumn.Type.PRIMARY_KEY));
    columns.add(DatabaseColumn.create("level", DatabaseDataType.INT));
    columns.add(DatabaseColumn.create("step", DatabaseDataType.INT));
    return new TutorialDatabaseTable(connection, keyspace, TABLE_NAME, columns);
  }

  private TutorialDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace, String name,
    List<DatabaseColumn> columns
  ) {
    super(connection, keyspace, name, columns);
  }

  public void insertTutorial(Tutorial tutorial) {
    insertTutorial(tutorial.user(), tutorial.level(), tutorial.step());
  }

  public void insertTutorial(UUID userId, int level, int step) {
    insert(DatabaseRow.of(userId, level, step));
  }

  public void updateTutorial(Tutorial tutorial) {
    update(DatabaseCell.create(tutorial.user()), DatabaseRow.of(tutorial.user(),
      tutorial.level(), tutorial.step()));
  }

  public CompletableFuture<Boolean> tutorialExists(UUID userId) {
    return exists(DatabaseCell.create(userId));
  }

  public void deleteTutorial(UUID userId) {
    delete(DatabaseCell.create(userId));
  }

  public CompletableFuture<Tutorial> findTutorial(UUID userId) {
    return selectRow(DatabaseCell.create(userId)).thenApply(Tutorial::of);
  }
}
