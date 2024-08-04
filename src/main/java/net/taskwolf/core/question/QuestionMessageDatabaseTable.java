package net.taskwolf.core.question;

import com.google.common.collect.Lists;
import net.taskwolf.core.database.*;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class QuestionMessageDatabaseTable extends DatabaseTable {
  private static final String TABLE_NAME = "question_message";

  public static QuestionMessageDatabaseTable create(
    DatabaseConnection connection, DatabaseKeyspace keyspace
  ) {
    var columns = Lists.<DatabaseColumn>newArrayList();
    columns.add(DatabaseColumn.create("id", DatabaseDataType.UUID,
      DatabaseColumn.Type.PRIMARY_KEY));
    columns.add(DatabaseColumn.create("sender", DatabaseDataType.TEXT));
    columns.add(DatabaseColumn.create("content", DatabaseDataType.TEXT));
    columns.add(DatabaseColumn.create("time", DatabaseDataType.BIGINT));
    return new QuestionMessageDatabaseTable(connection, keyspace, TABLE_NAME, columns);
  }

  private QuestionMessageDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace, String name,
    List<DatabaseColumn> columns
  ) {
    super(connection, keyspace, name, columns);
  }

  public CompletableFuture<Void> insertQuestionMessage(
    QuestionMessage questionMessage
  ) {
    return insertQuestionMessage(questionMessage.id(), questionMessage.sender(),
      questionMessage.content(), questionMessage.time());
  }

  public CompletableFuture<Void> insertQuestionMessage(
    UUID id, String sender, String content, long time
  ) {
    return insert(DatabaseRow.of(id, sender, content, time));
  }

  public CompletableFuture<UUID> generateAvailableMessageId() {
    var futureResponse = new CompletableFuture<UUID>();
    var id = UUID.randomUUID();
    questionMessageExists(id).thenApply(exists -> exists ?
      generateAvailableMessageId().thenApply(futureResponse::complete) :
      CompletableFuture.completedFuture(futureResponse.complete(id)));
    return futureResponse;
  }

  public CompletableFuture<Void> deleteQuestionMessage(UUID id) {
    return delete(DatabaseCell.create(id));
  }

  public CompletableFuture<Boolean> questionMessageExists(UUID id) {
    return exists(DatabaseCell.create(id));
  }

  public CompletableFuture<QuestionMessage> findQuestionMessage(UUID id) {
    return selectRow(DatabaseCell.create(id)).thenApply(QuestionMessage::of);
  }
}
