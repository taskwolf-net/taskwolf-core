package com.dulno.core.error;

import com.dulno.core.log.Log;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.io.PrintWriter;
import java.io.StringWriter;

@Singleton
@RequiredArgsConstructor(access = AccessLevel.PRIVATE, onConstructor = @__({@Inject}))
public final class ErrorRepository {
  private final ErrorDatabaseTable errorDatabaseTable;
  private final Log log;

  public void processError(Throwable throwable) {
    errorDatabaseTable.generateAvailableErrorId().thenCompose(id ->
      errorDatabaseTable.insertError(DulnoError.of(id, log.name(),
        System.getenv("POD_NAME"), System.getenv("CONTROLLER_NAME"),
        System.getenv("CONTROLLER_TYPE"), System.getenv("NODE_NAME"), throwable)));
    var stringWriter = new StringWriter();
    var printWriter = new PrintWriter(stringWriter);
    throwable.printStackTrace(printWriter);
    var lines = stringWriter.toString().split("\n");
    for (var line : lines) {
      log.warning(line.toString());
    }
  }
}
