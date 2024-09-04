package net.taskwolf.core.question;

import com.google.common.collect.Lists;
import net.taskwolf.core.database.*;
import net.taskwolf.core.database.condition.DatabaseCondition;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public final class QuestionDatabaseTable extends DatabaseTable {
  private static final String TABLE_NAME = "question";

  public static QuestionDatabaseTable create(
    DatabaseConnection connection, DatabaseKeyspace keyspace
  ) {
    var columns = Lists.<DatabaseColumn>newArrayList();
    columns.add(DatabaseColumn.create("requestMessage", DatabaseDataType.UUID,
      DatabaseColumn.Type.PRIMARY_KEY));
    columns.add(DatabaseColumn.create("sender", DatabaseDataType.TEXT));
    columns.add(DatabaseColumn.create("title", DatabaseDataType.TEXT));
    columns.add(DatabaseColumn.create("expirationTime", DatabaseDataType.BIGINT));
    columns.add(DatabaseListColumn.create("conversationMessages", DatabaseDataType.UUID));
    return new QuestionDatabaseTable(connection, keyspace, TABLE_NAME, columns);
  }

  private QuestionDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace, String name,
    List<DatabaseColumn> columns
  ) {
    super(connection, keyspace, name, columns);
  }

  public CompletableFuture<Void> insertQuestion(Question question) {
    return insertQuestion(question.requestMessage(), question.sender(),
      question.title(), question.expirationTime(), question.conversationMessages());
  }

  public CompletableFuture<Void> insertQuestion(
    UUID id,  String sender, String title, long expirationTime,
    List<UUID> conversationMessages
  ) {
    return insert(DatabaseRow.of(id, sender, title, expirationTime,
      conversationMessages));
  }

  public CompletableFuture<Void> addQuestionMessage(
    UUID id, UUID conversationMessage
  ) {
    var futureResponse = new CompletableFuture<Void>();
    findQuestion(id)
      .thenAccept(question -> addQuestionMessage(question, conversationMessage)
      .thenAccept(futureResponse::complete));
    return futureResponse;
  }

  private CompletableFuture<Void> addQuestionMessage(
    Question question, UUID conversationMessage
  ) {
    question.addConversationMessage(conversationMessage);
    return updateQuestion(question);
  }

  public CompletableFuture<Void> removeQuestionMessage(
    UUID id, UUID conversationMessage
  ) {
    var futureResponse = new CompletableFuture<Void>();
    findQuestion(id)
      .thenAccept(question -> removeQuestionMessage(question, conversationMessage)
      .thenAccept(futureResponse::complete));
    return futureResponse;
  }

  private CompletableFuture<Void> removeQuestionMessage(
    Question question, UUID conversationMessage
  ) {
    question.removeConversationMessage(conversationMessage);
    return updateQuestion(question);
  }

  public CompletableFuture<Void> resetQuestionExpirationTime(UUID id) {
    var futureResponse = new CompletableFuture<Void>();
    findQuestion(id).thenAccept(question -> resetQuestionExpirationTime(question)
      .thenAccept(futureResponse::complete));
    return futureResponse;
  }

  private CompletableFuture<Void> resetQuestionExpirationTime(Question question) {
    question.resetExpirationTime();
    return updateQuestion(question);
  }

  public CompletableFuture<Void> disableQuestionExpirationTime(UUID id) {
    var futureResponse = new CompletableFuture<Void>();
    findQuestion(id).thenAccept(question -> disableQuestionExpirationTime(question)
      .thenAccept(futureResponse::complete));
    return futureResponse;
  }

  private CompletableFuture<Void> disableQuestionExpirationTime(Question question) {
    question.disableExpirationTime();
    return updateQuestion(question);
  }

  public CompletableFuture<Void> updateQuestion(Question question) {
    return update(question.requestMessage(),
      DatabaseRow.of(question.requestMessage(), question.sender(),
        question.title(), question.expirationTime(),
        question.conversationMessages()));
  }

  public CompletableFuture<Void> deleteQuestion(UUID id) {
    return delete(id);
  }

  public CompletableFuture<Boolean> questionExists(UUID id) {
    return exists(id);
  }

  public CompletableFuture<Question> findQuestion(UUID id) {
    return selectRow(id).thenApply(Question::of);
  }

  public CompletableFuture<List<Question>> findQuestionsBySender(String sender) {
    return selectRows(DatabaseCondition.of("sender", sender))
      .thenApply(rows -> rows.stream().map(Question::of).toList());
  }

  public CompletableFuture<List<Question>> findAllQuestions() {
    return selectAllRows().thenApply(rows ->
      rows.stream().map(Question::of).collect(Collectors.toList()));
  }
}
