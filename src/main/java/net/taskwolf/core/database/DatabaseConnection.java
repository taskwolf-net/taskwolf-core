package net.taskwolf.core.database;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import com.datastax.oss.driver.api.core.CqlSession;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.taskwolf.core.log.Log;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

@Accessors(fluent = true)
@RequiredArgsConstructor(staticName = "create")
public final class DatabaseConnection {
  private final DatabaseConfiguration databaseConfiguration;
  private final Log log;
  @Getter
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
}
