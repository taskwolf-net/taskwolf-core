package net.taskwolf.core.database.transformation;

import net.taskwolf.core.database.DatabaseRow;

public interface DatabaseTransformation {
  /**
   * Is used to transform a {@link DatabaseRow} from the old format into the
   * new format
   * @param oldRow A {@link DatabaseRow} in the old format
   * @return A {@link DatabaseRow} in the new format
   */
  DatabaseRow transformOldToNew(DatabaseRow oldRow);

  /**
   * Is used to transform a {@link DatabaseRow} from the new format into the
   * old format
   * @param newRow A {@link DatabaseRow} in the new format
   * @return A {@link DatabaseRow} in the old format
   */
  DatabaseRow transformNewToOld(DatabaseRow newRow);
}
