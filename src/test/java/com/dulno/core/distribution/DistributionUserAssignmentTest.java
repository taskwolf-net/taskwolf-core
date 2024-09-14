package com.dulno.core.distribution;

import com.google.inject.Guice;
import com.dulno.core.CoreInjectionModule;
import com.dulno.core.worker.WorkerUserAssignment;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.UUID;

final class DistributionUserAssignmentTest {
  @Test
  void testDistributionUserAssignment() {
    var injector = Guice.createInjector(CoreInjectionModule.create());
    var assignment = injector.getInstance(WorkerUserAssignment.class);
    var user = UUID.randomUUID();
    assignment.assignUser("test", user);
    var assignedUsers = assignment.findAssignedUsers("test");
    Assertions.assertTrue(assignedUsers.contains(user));
    Assertions.assertEquals(assignedUsers.size(), 1);
    assignment.removeUser("test", user);
    assignedUsers = assignment.findAssignedUsers("test");
    Assertions.assertFalse(assignedUsers.contains(user));
    Assertions.assertTrue(assignedUsers.isEmpty());
  }
}
