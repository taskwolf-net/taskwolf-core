package net.taskwolf.core.trial;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.taskwolf.core.database.DatabaseConnection;
import net.taskwolf.core.database.DatabaseKeyspace;
import net.taskwolf.core.stripe.StripeDatabaseTable;

@RequiredArgsConstructor(staticName = "create")
public final class TrialInjectionModule extends AbstractModule {
  @Provides
  @Singleton
  TrialDatabaseTable provideTrialDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace
  ) {
    var trialDatabaseTable = TrialDatabaseTable.create(connection, keyspace);
    trialDatabaseTable.createIfNotExists();
    return trialDatabaseTable;
  }
}
