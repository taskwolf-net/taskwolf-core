package com.dulno.core.database.skeleton;

public interface IndexableDatabaseTable extends AbstractDatabaseTable {

  /**
   * Creates an index for a column of the database table even if it already exists
   * @param column The column for which the index is to be created
   */
  default void createIndex(String column) {
    createIndex(column, "", "");
  }

  /**
   * Creates an index for a column of the database table even if it already exists
   * @param column The column for which the index is to be created
   * @param customType The custom type of the index
   */
  default void createIndex(String column, String customType) {
    createIndex(column, "", customType);
  }

  /**
   * Creates an index for a column of the database table if it does not already exist
   * @param column The column for which the index is to be created
   */
  default void createIndexIfNotExists(String column) {
    createIndex(column, "IF NOT EXISTS", "");
  }

  /**
   * Creates an index for a column of the database table if it does not already exist
   * @param column The column for which the index is to be created
   * @param customType The custom type of the index
   */
  default void createIndexIfNotExists(String column, String customType) {
    createIndex(column, "IF NOT EXISTS", customType);
  }

  private void createIndex(String column, String addition, String customType) {
    var query = new StringBuilder("CREATE");
    if (!customType.isEmpty()) {
      query.append(" CUSTOM");
    }
    query.append(" INDEX ");
    query.append(addition);
    query.append(" ON ");
    query.append(fullName());
    query.append(" (");
    query.append(column);
    query.append(")");
    if (!customType.isEmpty()) {
      query.append(" USING ");
      query.append(customType);
    }
    query.append(";");
    connection().executesSynchronously(query);
  }
}
