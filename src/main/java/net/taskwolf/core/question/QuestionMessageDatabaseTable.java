package net.taskwolf.core.question;

import net.taskwolf.core.database.*;
import net.taskwolf.core.database.condition.DatabaseCondition;
import com.google.common.collect.Lists;

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
    columns.add(DatabaseColumn.create("publicId", DatabaseDataType.TEXT));
    columns.add(DatabaseColumn.create("questionId", DatabaseDataType.UUID));
    columns.add(DatabaseColumn.create("sender", DatabaseDataType.TEXT));
    columns.add(DatabaseColumn.create("senderType", DatabaseDataType.TEXT));
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
    return insertQuestionMessage(questionMessage.id(), questionMessage.publicId(),
      questionMessage.questionId(), questionMessage.sender(),
      questionMessage.senderType(), questionMessage.content(),
      questionMessage.time());
  }

  public CompletableFuture<Void> insertQuestionMessage(
    UUID id, String publicId, UUID questionId, String sender,
    QuestionMessageSenderType senderType, String content, long time
  ) {
    return insert(DatabaseRow.of(id, publicId, questionId, sender,
      senderType.toString(), content, time));
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
    return delete(id);
  }

  public CompletableFuture<Boolean> questionMessageExists(UUID id) {
    return exists(id);
  }

  public CompletableFuture<Boolean> questionMessageExists(String publicId) {
    return exists(DatabaseCondition.of("publicId", publicId));
  }

  public CompletableFuture<QuestionMessage> findQuestionMessage(UUID id) {
    return selectRow(id).thenApply(QuestionMessage::of);
  }

  public CompletableFuture<QuestionMessage> findQuestionMessage(String publicId) {
    return selectRow(DatabaseCondition.of("publicId", publicId))
      .thenApply(QuestionMessage::of);
  }

  public CompletableFuture<List<QuestionMessage>> findMessagesOfQuestion(
    UUID questionId
  ) {
    return selectRows(DatabaseCondition.of("questionId", questionId))
      .thenApply(rows -> rows.stream().map(QuestionMessage::of).toList());
  }
}
