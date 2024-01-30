package net.taskwolf.core.distribution.server.node;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import net.taskwolf.core.event.EventHook;
import net.taskwolf.core.event.Hook;
import net.taskwolf.core.event.node.NodeModuleLoadEvent;

@Singleton
@RequiredArgsConstructor(access = AccessLevel.PRIVATE, onConstructor = @__({@Inject}))
public final class NodeModuleLoadHook implements Hook {
  @EventHook
  private void nodeModuleLoad(NodeModuleLoadEvent event) {
    event.client().condition().addModule(event.module());
  }
}
