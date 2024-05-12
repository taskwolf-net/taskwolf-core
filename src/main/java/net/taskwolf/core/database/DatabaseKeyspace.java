package net.taskwolf.core.database;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(staticName = "create")
public final class DatabaseKeyspace {
  private final DatabaseConnection connection;
  private final String name;
  private final String replicationClass;
  private final int replicationFactor;

  /**
   * Creates keyspace even if it already exists
   */
  public void create() {
    create("");
  }

  /**
   * Creates keyspace only if it not already exists
   */
  public void createIfNotExists() {
    create("IF NOT EXISTS ");
  }

  private void create(String addition) {
    var query = new StringBuilder("CREATE KEYSPACE ");
    query.append(addition);
    query.append(name);
    query.append(" WITH REPLICATION = {'class' : '");
    query.append(replicationClass);
    query.append("', 'replication_factor' : ");
    query.append(replicationFactor);
    query.append("};");
    connection.session().executeAsync(query.toString());
  }

  /**
   * Deletes the keyspace and all its content
   */
  public void drop() {
    drop("");
  }

  /**
   * Deletes the keyspace and all its content only if it exists
   */
  public void dropIfExists() {
    drop("IF EXISTS ");
  }

  private void drop(String addition) {
    var query = new StringBuilder("DROP KEYSPACE ");
    query.append(addition);
    query.append(name);
    query.append(";");
    connection.session().executeAsync(query.toString());
  }

  /**
   * Switches the currently active keyspace to this one
   */
  public void use() {
    var query = new StringBuilder("USE ");
    query.append(name);
    query.append(";");
    connection.session().executeAsync(query.toString());
  }
}
