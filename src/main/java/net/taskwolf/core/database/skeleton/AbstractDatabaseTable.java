package net.taskwolf.core.database.skeleton;

import com.datastax.oss.driver.api.core.cql.AsyncResultSet;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import net.taskwolf.core.database.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface AbstractDatabaseTable {
  /**
   * The implemented database table
   * @return The table
   */
  DatabaseTable table();

  /**
   * Is used to find the connection of the table
   * @return The connection of the table
   */
  DatabaseConnection connection();

  /**
   * Is used to find the keyspace in which the table is located
   * @return The keyspace
   */
  DatabaseKeyspace keyspace();

  /**
   * Returns the name of the table
   * @return The name
   */
  String name();

  /**
   * Is used to construct the full name of the table (keyspace and name combined)
   * @return The full name of the table
   */
  String fullName();

  /**
   * Is used to find the columns of the table
   * @return The columns
   */
  List<DatabaseColumn> columns();

  /**
   * Creates a compilation of the names of the columns
   * @return The name compilation
   */
  String columnNameCompilation();

  /**
   * Another way to create a column name compilation with more unique parameters
   * @param columns The columns from which the compilation in created
   * @param prefix The prefix of the compilation
   * @param suffix The suffix of the compilation
   * @return The column name compilation
   */
  String columnNameCompilation(
    List<DatabaseColumn> columns, String prefix, String suffix
  );

  /**
   * Is used to find a single primary key column
   * @return The single primary key column
   */
  DatabaseColumn findPrimaryKeyColumn();

  /**
   * Is used to find all column with type primary key
   * @return The list of columns
   */
  List<DatabaseColumn> findPrimaryKeyColumns();

  /**
   * Is used to find a single partition key column
   * @return The single partition key column
   */
  DatabaseColumn findPartitionKeyColumn();

  /**
   * Is used to find all column with type partition key
   * @return The list of columns
   */
  List<DatabaseColumn> findPartitionKeyColumns();

  /**
   * Is used to execute a cql query
   * @param accessType The access type used for transformation
   * @param stringBuilder The string builder that contains the query
   * @param values The placeholder values
   * @return The future that contains the result set
   */
  CompletableFuture<AsyncResultSet> execute(DatabaseAccessType accessType,
    StringBuilder stringBuilder, Object... values);

  /**
   * Is used to execute a cql query
   * @param accessType The access type used for transformation
   * @param query The query
   * @param values The placeholder values
   * @return The future that contains the result set
   */
  CompletableFuture<AsyncResultSet> execute(DatabaseAccessType accessType,
    String query, Object... values);

  /**
   * Is used to execute a cql query
   * @param accessType The access type used for transformation
   * @param simpleStatement The statement that is to be executed
   * @param values The placeholder values
   * @return The future that contains the result set
   */
  CompletableFuture<AsyncResultSet> execute(DatabaseAccessType accessType,
    SimpleStatement simpleStatement, Object... values);
}
