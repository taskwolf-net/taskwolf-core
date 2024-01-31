package net.taskwolf.core.distribution;

public enum NodeType {
  WORKER,
  PROXY;

  public boolean isWorker() {
    return this == WORKER;
  }

  public boolean isProxy() {
    return this == PROXY;
  }
}
