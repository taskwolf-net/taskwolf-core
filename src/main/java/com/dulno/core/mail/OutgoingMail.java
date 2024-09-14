package com.dulno.core.mail;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import com.dulno.core.database.DatabaseRow;

import java.util.UUID;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(staticName = "create")
public final class OutgoingMail {
  public static OutgoingMail of(DatabaseRow row) {
    return create(row.findCell(0).uuidValue(),
      row.findCell(1).stringValue(), row.findCell(2).stringValue(),
      row.findCell(3).longValue(), row.findCell(4).stringValue(),
      row.findCell(5).blobValue().array());
  }

  private final UUID id;
  private final String receiver;
  private final String sender;
  private final long time;
  private final String title;
  private final byte[] content;
}
