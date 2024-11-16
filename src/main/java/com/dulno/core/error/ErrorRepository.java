package com.dulno.core.error;

import com.dulno.core.log.Log;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@Singleton
@RequiredArgsConstructor(access = AccessLevel.PRIVATE, onConstructor = @__({@Inject}))
public final class ErrorRepository {
  private final ErrorDatabaseTable errorDatabaseTable;
  private final Log log;

  public void processError(Throwable throwable) {
    errorDatabaseTable.insertError(throwable);
    for (var element : throwable.getStackTrace()) {
      log.severe(element.toString());
    }
  }
}
