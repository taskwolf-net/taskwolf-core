package com.dulno.core.question;

import com.dulno.core.database.*;
import com.google.common.collect.Lists;
import com.dulno.core.database.condition.DatabaseCondition;

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
    columns.add(DatabaseColumn.create("id", DatabaseDataType.UUID,
      DatabaseColumn.Type.PRIMARY_KEY));
    columns.add(DatabaseColumn.create("sender", DatabaseDataType.TEXT));
    columns.add(DatabaseColumn.create("title", DatabaseDataType.TEXT));
    columns.add(DatabaseColumn.create("status", DatabaseDataType.TEXT));
    columns.add(DatabaseColumn.create("expirationTime", DatabaseDataType.BIGINT));
    return new QuestionDatabaseTable(connection, keyspace, TABLE_NAME, columns);
  }

  private QuestionDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace, String name,
    List<DatabaseColumn> columns
  ) {
    super(connection, keyspace, name, columns);
  }

  public CompletableFuture<Void> insertQuestion(Question question) {
    return insertQuestion(question.id(), question.sender(),
      question.title(), question.status().toString(), question.expirationTime());
  }

  public CompletableFuture<Void> insertQuestion(
    UUID id,  String sender, String title, String status, long expirationTime
  ) {
    return insert(DatabaseRow.of(id, sender, title, status, expirationTime));
  }

  public CompletableFuture<Void> updateQuestionStatus(
    UUID id, Question.Status status
  ) {
    var futureResponse = new CompletableFuture<Void>();
    findQuestion(id).thenAccept(question -> updateQuestionStatus(question, status)
      .thenAccept(futureResponse::complete));
    return futureResponse;
  }

  private CompletableFuture<Void> updateQuestionStatus(
    Question question, Question.Status status
  ) {
    question.updateStatus(status);
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
    return update(question.id(), DatabaseRow.of(question.id(), question.sender(),
      question.title(), question.expirationTime()));
  }

  public CompletableFuture<UUID> generateAvailableQuestionId() {
    var futureResponse = new CompletableFuture<UUID>();
    var id = UUID.randomUUID();
    questionExists(id).thenApply(exists -> exists ?
      generateAvailableQuestionId().thenApply(futureResponse::complete) :
      CompletableFuture.completedFuture(futureResponse.complete(id)));
    return futureResponse;
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

  public CompletableFuture<List<Question>> findOpenQuestions() {
    return selectRows(DatabaseCondition.of("status", Question.Status.OPEN.toString()))
      .thenApply(rows -> rows.stream().map(Question::of).collect(Collectors.toList()));
  }
}
