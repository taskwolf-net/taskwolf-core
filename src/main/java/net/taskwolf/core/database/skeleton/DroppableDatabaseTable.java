package net.taskwolf.core.database.skeleton;

public interface DroppableDatabaseTable extends AbstractDatabaseTable {

  /**
   * Deletes the database table and all its content
   */
  default void drop() {
    drop("");
  }

  /**
   * Deletes the database table and all its content only if it exists
   */
  default void dropIfExists() {
    drop("IF EXISTS ");
  }

  default void drop(String addition) {
    var query = new StringBuilder("DROP TABLE ");
    query.append(addition);
    query.append(fullName());
    query.append(";");
    connection().execute(query);
  }
}
