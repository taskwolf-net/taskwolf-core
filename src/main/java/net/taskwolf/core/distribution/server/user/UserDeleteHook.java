package net.taskwolf.core.distribution.server.user;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import net.taskwolf.core.distribution.DistributionUserAssignment;
import net.taskwolf.core.event.EventHook;
import net.taskwolf.core.event.Hook;
import net.taskwolf.core.event.user.UserDeleteEvent;

@Singleton
@RequiredArgsConstructor(access = AccessLevel.PRIVATE, onConstructor = @__({@Inject}))
public final class UserDeleteHook implements Hook {
  private final DistributionUserAssignment userAssignment;

  @EventHook
  private void userDelete(UserDeleteEvent event) {
    var user = event.user();
    userAssignment.findModulesAssignedTo(user).forEach(module ->
      userAssignment.removeUser(module, user));
  }
}
