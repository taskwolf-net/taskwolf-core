package com.dulno.core.question;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import com.dulno.core.database.DatabaseRow;

import java.util.UUID;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(staticName = "create")
public final class QuestionMessage {
  public static QuestionMessage of(DatabaseRow row) {
    return create(row.findCell(0).uuidValue(), row.findCell(1).stringValue(),
      row.findCell(2).uuidValue(), row.findCell(3).stringValue(),
      QuestionMessageSenderType.valueOf(row.findCell(4).stringValue()),
      row.findCell(5).stringValue(), row.findCell(6).longValue());
  }

  private final UUID id;
  private final String publicId;
  private final UUID questionId;
  private final String sender;
  private final QuestionMessageSenderType senderType;
  private final String content;
  private final long time;
}
