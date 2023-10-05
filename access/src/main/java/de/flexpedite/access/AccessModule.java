package de.flexpedite.access;

import com.google.inject.AbstractModule;
import de.flexpedite.core.log.Log;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(staticName = "create")
public final class AccessModule extends AbstractModule {
  @Override
  protected void configure() {
    /*bind(int.class).toInstance(-1);
    bind(float.class).toInstance(-1F);
    bind(int[].class).toInstance(new int[0]);*/
    //install(AccountModule.create());
    configureLog();
  }

  private void configureLog() {
    try {
      bind(Log.class).toInstance(Log.create("Access", "/logs/access/"));
    } catch (Exception exception) {
      exception.printStackTrace();
    }
  }
}
