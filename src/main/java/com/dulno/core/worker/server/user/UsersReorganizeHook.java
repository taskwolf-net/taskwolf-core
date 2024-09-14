package com.dulno.core.worker.server.user;

import com.dulno.core.worker.event.user.UsersReorganizeEvent;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import com.dulno.core.event.EventHook;
import com.dulno.core.event.Hook;
import com.dulno.core.worker.WorkerUserAssignment;

@Singleton
@RequiredArgsConstructor(access = AccessLevel.PRIVATE, onConstructor = @__({@Inject}))
public final class UsersReorganizeHook implements Hook {
  private final WorkerUserAssignment userAssignment;

  @EventHook
  private void userReorganize(UsersReorganizeEvent event) {
    var module = event.module();
    userAssignment.deleteModule(module);
    userAssignment.assignUsers(module, event.users());
  }
}
