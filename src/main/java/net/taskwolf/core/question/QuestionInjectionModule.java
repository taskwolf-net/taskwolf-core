package net.taskwolf.core.question;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.taskwolf.core.database.DatabaseConnection;
import net.taskwolf.core.database.DatabaseKeyspace;

@RequiredArgsConstructor(staticName = "create")
public final class QuestionInjectionModule extends AbstractModule {
  @Provides
  @Singleton
  QuestionDatabaseTable provideQuestionDatabaseTable(
    DatabaseConnection databaseConnection, DatabaseKeyspace databaseKeyspace
  ) {
    var questionDatabaseTable = QuestionDatabaseTable.create(
      databaseConnection, databaseKeyspace);
    questionDatabaseTable.createIfNotExists();
    return questionDatabaseTable;
  }

  @Provides
  @Singleton
  QuestionMessageDatabaseTable provideQuestionMessageDatabaseTable(
    DatabaseConnection databaseConnection, DatabaseKeyspace databaseKeyspace
  ) {
    var questionMessageDatabaseTable = QuestionMessageDatabaseTable.create(
      databaseConnection, databaseKeyspace);
    questionMessageDatabaseTable.createIfNotExists();
    return questionMessageDatabaseTable;
  }
}
