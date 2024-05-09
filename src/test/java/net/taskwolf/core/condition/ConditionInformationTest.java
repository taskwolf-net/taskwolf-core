package net.taskwolf.core.condition;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

final class ConditionInformationTest {
  @Test
  void testConditionInformation() {
    var information = ConditionInformation.builder()
      .withName("Test")
      .withDataType(ConditionDataType.TEXT)
      .withIdentifier("test-identifier")
      .build();
    Assertions.assertEquals(information.name(), "Test");
    Assertions.assertEquals(information.dataType(), ConditionDataType.TEXT);
    Assertions.assertEquals(information.identifier(), "test-identifier");
  }
}