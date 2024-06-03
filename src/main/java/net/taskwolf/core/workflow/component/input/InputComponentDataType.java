package net.taskwolf.core.workflow.component.input;

/**
 * This enum is used to define the data type of an input variable.
 * The input field in the web app is adapted for different input types.
 */
public enum InputComponentDataType {
  TEXT,
  NUMBER,
  BOOLEAN,
  UUID,
  DATE,
  SELECT,
  FILE;
}
