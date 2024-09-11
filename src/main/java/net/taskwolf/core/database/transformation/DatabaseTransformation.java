package net.taskwolf.core.database.transformation;

import net.taskwolf.core.database.DatabaseColumn;
import net.taskwolf.core.database.DatabaseRow;
import net.taskwolf.core.database.DatabaseTable;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface DatabaseTransformation {
  /**
   * Is used to transform a {@link DatabaseRow} from the old format into the
   * new format
   * @param oldRow A {@link DatabaseRow} in the old format
   * @return A future that contains the {@link DatabaseRow} in the new format
   */
  CompletableFuture<DatabaseRow> transformOldToNew(DatabaseRow oldRow);

  /**
   * Is used to transform a {@link DatabaseRow} from the new format into the
   * old format
   * @param newRow A {@link DatabaseRow} in the new format
   * @return A future that contains the {@link DatabaseRow} in the old format
   */
  CompletableFuture<DatabaseRow> transformNewToOld(DatabaseRow newRow);

  /**
   * The old columns of the table before the transformation
   * @return The list of old columns
   */
  List<DatabaseColumn> oldColumns();

  /**
   * This function is called to create indexes for the new table
   * @param newTable The new table for which the indexes are created
   */
  void initializeNewTableIndexes(DatabaseTable newTable);

  /**
   * This function is called to create views for the new table
   * @param newTable The new table for which the views are created
   */
  void initializeNewTableViews(DatabaseTable newTable);
}
