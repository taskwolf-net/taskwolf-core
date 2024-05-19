package net.taskwolf.core.database;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import java.util.concurrent.CompletableFuture;

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
   * @return A future result that is completed when creation is completed
   */
  public CompletableFuture<Void> create() {
    return create("");
  }

  /**
   * Creates keyspace only if it not already exists
   * @return A future result that is completed when creation is completed
   */
  public CompletableFuture<Void> createIfNotExists() {
    return create("IF NOT EXISTS ");
  }

  private CompletableFuture<Void> create(String addition) {
    var query = new StringBuilder("CREATE KEYSPACE ");
    query.append(addition);
    query.append(name);
    query.append(" WITH REPLICATION = {'class' : '");
    query.append(replicationClass);
    query.append("', 'replication_factor' : ");
    query.append(replicationFactor);
    query.append("};");
    var result = connection.session().executeAsync(query.toString());
    var futureResponse = new CompletableFuture<Void>();
    result.thenAccept(resultSet -> futureResponse.complete(null));
    return futureResponse;
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
