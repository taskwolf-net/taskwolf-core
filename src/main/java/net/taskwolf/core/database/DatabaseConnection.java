package net.taskwolf.core.database;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import com.datastax.oss.driver.api.core.CqlSession;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.slf4j.LoggerFactory;

@Accessors(fluent = true)
@RequiredArgsConstructor(staticName = "create")
public final class DatabaseConnection {
  @Getter
  private CqlSession session;

  public void connect() {
    ((LoggerContext) LoggerFactory.getILoggerFactory())
      .getLogger("com.datastax").setLevel(Level.ERROR);
    session = CqlSession.builder().build();
  }
}
