package net.taskwolf.core.distribution.server.user;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import net.taskwolf.core.distribution.DistributionUserAssignment;
import net.taskwolf.core.event.EventHook;
import net.taskwolf.core.event.Hook;
import net.taskwolf.core.event.user.UsersReorganizeEvent;

@Singleton
@RequiredArgsConstructor(access = AccessLevel.PRIVATE, onConstructor = @__({@Inject}))
public final class UsersReorganizeHook implements Hook {
  private final DistributionUserAssignment userAssignment;

  @EventHook
  private void userReorganize(UsersReorganizeEvent event) {
    var module = event.module();
    userAssignment.deleteModule(module);
    userAssignment.assignUsers(module, event.users());
  }
}
