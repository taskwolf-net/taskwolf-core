package net.taskwolf.core;

import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import net.taskwolf.core.database.DatabaseColumn;
import net.taskwolf.core.database.DatabaseDataType;
import net.taskwolf.core.database.DatabaseRow;
import net.taskwolf.core.database.DatabaseTable;
import net.taskwolf.core.database.transformation.DatabaseTransformation;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor(staticName = "create")
public final class TestDatabaseTableTransformation implements DatabaseTransformation {
  @Override
  public CompletableFuture<DatabaseRow> transformOldToNew(DatabaseRow oldRow) {
    return CompletableFuture.completedFuture(DatabaseRow.of(
      oldRow.findCell(0).rawValue(), "NAME", oldRow.findCell(1).rawValue()));
  }

  @Override
  public CompletableFuture<DatabaseRow> transformNewToOld(DatabaseRow newRow) {
    return CompletableFuture.completedFuture(DatabaseRow.of(
      newRow.findCell(0).rawValue(), newRow.findCell(2).rawValue()));
  }

  @Override
  public List<DatabaseColumn> oldColumns() {
    var columns = Lists.<DatabaseColumn>newArrayList();
    columns.add(DatabaseColumn.create("id", DatabaseDataType.UUID,
      DatabaseColumn.Type.PARTITION_KEY));
    columns.add(DatabaseColumn.create("email", DatabaseDataType.TEXT));
    return columns;
  }

  @Override
  public void initializeNewTableIndexes(DatabaseTable newTable) {
    newTable.createIndexIfNotExists("email");
    newTable.createIndexIfNotExists("name");
  }

  @Override
  public void initializeNewTableViews(DatabaseTable newTable) {
    newTable.createMaterializedViewIfNotExists("email_view", "email");
    newTable.createMaterializedViewIfNotExists("name_view", "name");
  }
}
