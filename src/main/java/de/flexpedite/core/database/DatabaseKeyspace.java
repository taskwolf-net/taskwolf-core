package de.flexpedite.core.database;

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

  public void create() {
    create("");
  }

  public void createIfNotExists() {
    create("IF NOT EXISTS ");
  }

  private void create(String addition) {
    StringBuilder query = new StringBuilder("CREATE TABLE ");
    query.append(addition);
    query.append(name);
    query.append("WITH REPLICATION = {\'class\' : \'");
    query.append(replicationClass);
    query.append("', 'replication_factor' : ");
    query.append(replicationFactor);
    query.append("};");
    connection.session().executeAsync(query.toString());
  }

  public void drop() {
    drop("");
  }

  public void dropIfExists() {
    drop("IF EXISTS ");
  }

  private void drop(String addition) {
    StringBuilder query = new StringBuilder("DROP KEYSPACE ");
    query.append(addition);
    query.append(name);
    query.append(";");
    connection.session().executeAsync(query.toString());
  }

  public void use() {
    StringBuilder query = new StringBuilder("USE ");
    query.append(name);
    query.append(";");
    connection.session().executeAsync(query.toString());
  }
}
