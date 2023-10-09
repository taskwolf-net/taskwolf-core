package de.flexpedite.core.database;

import com.datastax.oss.driver.api.core.CqlSession;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@Accessors(fluent = true)
@RequiredArgsConstructor(staticName = "create")
public final class DatabaseConnection {
  @Getter
  private CqlSession session;

  public void connect() {
    session = CqlSession.builder().build();
  }
}
