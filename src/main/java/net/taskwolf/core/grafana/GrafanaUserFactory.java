package net.taskwolf.core.grafana;


import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import net.taskwolf.core.whitelist.WhitelistConfiguration;

import java.util.UUID;

@Singleton
@RequiredArgsConstructor(access = AccessLevel.PRIVATE, onConstructor = @__({@Inject}))
public final class GrafanaUserFactory {
  private final GrafanaConfiguration grafanaConfiguration;
  private final GrafanaDatabaseTable grafanaDatabaseTable;
  private final WhitelistConfiguration whitelistConfiguration;

  public GrafanaUser createUser(UUID ownerId) {
    return GrafanaUser.create(grafanaConfiguration, grafanaDatabaseTable,
      whitelistConfiguration, ownerId);
  }
}
