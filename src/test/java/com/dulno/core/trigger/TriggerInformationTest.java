package com.dulno.core.trigger;

import com.dulno.core.workflow.component.ComponentNovelty;
import com.dulno.core.workflow.component.input.InputComponentDataType;
import com.dulno.core.workflow.component.input.InputComponentVariable;
import com.dulno.core.workflow.component.output.OutputComponentVariable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

final class TriggerInformationTest {
  @Test
  void testTriggerInformation() {
    var information = TriggerInformation.builder()
      .withName("Test")
      .withDescription("Test")
      .withNovelty(ComponentNovelty.NEW)
      .withInputVariable(InputComponentVariable.createRequired("Input",
        "input-identifier", "Description", InputComponentDataType.TEXT))
      .withOutputVariable(OutputComponentVariable.create("Output",
        "output-identifier"))
      .build();
    Assertions.assertEquals(information.name(), "Test");
    Assertions.assertEquals(information.description(), "Test");
    Assertions.assertEquals(information.novelty(), ComponentNovelty.NEW);
    Assertions.assertEquals(information.inputVariables().size(), 1);
    Assertions.assertEquals(information.inputVariables().get(0).identifier(),
      "input-identifier");
    Assertions.assertEquals(information.outputVariables().size(), 1);
    Assertions.assertEquals(information.outputVariables().get(0).identifier(),
      "output-identifier");
  }
}
