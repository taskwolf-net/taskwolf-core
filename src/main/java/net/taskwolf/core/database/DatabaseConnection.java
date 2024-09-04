package net.taskwolf.core.database;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.AsyncResultSet;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.taskwolf.core.log.Log;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@RequiredArgsConstructor(staticName = "create")
public final class DatabaseConnection {
  private final DatabaseConfiguration databaseConfiguration;
  private final Log log;
  private CqlSession session;

  /**
   * Used to connect to cassandra database
   */
  public void connect() {
    try {
      ((LoggerContext) LoggerFactory.getILoggerFactory())
        .getLogger("com.datastax").setLevel(Level.ERROR);
      session = CqlSession.builder()
        .addContactPoint(new InetSocketAddress(databaseConfiguration.hostname(),
          databaseConfiguration.port()))
        .withLocalDatacenter(databaseConfiguration.datacenter()).build();
    } catch (Exception exception) {
      log.severe("The connection to cassandra failed");
      System.exit(0);
    }
  }

  /**
   * Is used to execute a cql query
   * @param stringBuilder The string builder that contains the query
   * @param values The placeholder values
   * @return The future that contains the result set
   */
  public CompletableFuture<AsyncResultSet> execute(
    StringBuilder stringBuilder, Object... values
  ) {
    return execute(stringBuilder.toString(), values);
  }

  /**
   * Is used to execute a cql query
   * @param query The query
   * @param values The placeholder values
   * @return The future that contains the result set
   */
  public CompletableFuture<AsyncResultSet> execute(
    String query, Object... values
  ) {
    return session.prepareAsync(query)
      .thenApply(statement -> statement.bind(values))
      .thenCompose(statement -> session.executeAsync(statement))
      .toCompletableFuture();
  }

  /**
   * Is used to execute a cql query
   * @param simpleStatement The statement that is to be executed
   * @param values The placeholder values
   * @return The future that contains the result set
   */
  public CompletableFuture<AsyncResultSet> execute(
    SimpleStatement simpleStatement, Object... values
  ) {
    return session.prepareAsync(simpleStatement)
      .thenApply(statement -> statement.bind(values))
      .thenCompose(statement -> session.executeAsync(statement))
      .toCompletableFuture();
  }
}
