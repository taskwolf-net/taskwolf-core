package net.taskwolf.core;

import com.google.inject.Guice;
import com.google.inject.Injector;
import net.taskwolf.core.command.CommandRegistry;
import net.taskwolf.core.command.CommandTask;
import net.taskwolf.core.command.implementation.*;
import net.taskwolf.core.condition.ConditionInformationRepository;
import net.taskwolf.core.condition.number.ConditionNumberGreaterThan;
import net.taskwolf.core.condition.number.ConditionNumberSmallerThan;
import net.taskwolf.core.condition.text.ConditionTextEndsWith;
import net.taskwolf.core.condition.text.ConditionTextEquals;
import net.taskwolf.core.condition.text.ConditionTextStartsWith;
import net.taskwolf.core.distribution.DistributionConfiguration;
import net.taskwolf.core.distribution.client.DistributionClient;
import net.taskwolf.core.distribution.client.DistributionClientRegistry;
import net.taskwolf.core.distribution.client.packet.PacketOutgoingHandshakeRequest;
import net.taskwolf.core.distribution.server.DistributionServer;
import net.taskwolf.core.distribution.server.node.*;
import net.taskwolf.core.distribution.packet.PacketRegistry;
import net.taskwolf.core.distribution.server.packet.*;
import net.taskwolf.core.event.EventExecutor;
import net.taskwolf.core.event.HookRegistry;
import net.taskwolf.core.intro.Intro;
import net.taskwolf.core.log.Log;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"net.taskwolf"},
  exclude = {org.springframework.boot.autoconfigure.gson.GsonAutoConfiguration.class})
public class CoreApplication {
  public static void main(String[] args) throws Exception {
    var injector = Guice.createInjector(CoreInjectionModule.create());
    Intro.create("1.0.0").print();
    var log = injector.getInstance(Log.class);
    log.info("Initializing Taskwolf - Core");
    var application = injector.getInstance(SpringApplication.class);
    registerConditions(injector.getInstance(ConditionInformationRepository.class));
    var packetRegistry = injector.getInstance(PacketRegistry.class);
    registerDistributionPackets(packetRegistry);
    var hookRegistry = injector.getInstance(HookRegistry.class);
    registerHooks(hookRegistry, injector);
    var distributionConfiguration = injector.getInstance(DistributionConfiguration.class);
    setupDistribution(distributionConfiguration, packetRegistry,
      injector.getInstance(EventExecutor.class),
      injector.getInstance(DistributionClientRegistry.class));
    var nodePingScheduler = injector.getInstance(NodePingSchedule.class);
    nodePingScheduler.start();
    var coreModule = injector.getInstance(CoreModule.class);
    coreModule.initialize();
    var commandRegistry = injector.getInstance(CommandRegistry.class);
    registerCommands(commandRegistry, injector);
    application.run(args);
    new Thread(() -> CommandTask.create(log, commandRegistry).start()).start();
    log.info("Successfully booted Taskwolf - Core");
  }

  private static void registerConditions(ConditionInformationRepository repository) {
    repository.register(ConditionTextEquals.information());
    repository.register(ConditionTextStartsWith.information());
    repository.register(ConditionTextEndsWith.information());
    repository.register(ConditionNumberGreaterThan.information());
    repository.register(ConditionNumberSmallerThan.information());
  }

  private static void registerDistributionPackets(
    PacketRegistry registry
  ) throws Exception {
    registry.registerPacket(PacketIncomingHandshakeRequest.class);
    registry.registerPacket(PacketIncomingHandshakeResponse.class);
    registry.registerPacket(PacketIncomingPing.class);
    registry.registerPacket(PacketIncomingPong.class);
    registry.registerPacket(PacketIncomingDisconnect.class);
  }

  private static void registerHooks(HookRegistry hookRegistry, Injector injector) {
    hookRegistry.register(injector.getInstance(NodeHandshakeRequestHook.class));
    hookRegistry.register(injector.getInstance(NodeHandshakeResponseHook.class));
    hookRegistry.register(injector.getInstance(NodePingHook.class));
    hookRegistry.register(injector.getInstance(NodePongHook.class));
    hookRegistry.register(injector.getInstance(NodeDisconnectHook.class));
  }

  private static void setupDistribution(
    DistributionConfiguration configuration, PacketRegistry registry,
    EventExecutor eventExecutor, DistributionClientRegistry clientRegistry
  ) {
    var server = DistributionServer.create(configuration, registry,
      eventExecutor, clientRegistry, configuration.self().distributionPort());
    server.openAsync(() -> connectToNodes(configuration, registry, eventExecutor,
      clientRegistry));
  }

  private static void connectToNodes(
    DistributionConfiguration configuration, PacketRegistry registry,
    EventExecutor eventExecutor, DistributionClientRegistry clientRegistry
  ) {
    for (var node : configuration.nodes()) {
      var client = DistributionClient.create(configuration, registry,
        eventExecutor, clientRegistry, node);
      clientRegistry.registerClient(client);
      client.connectAsync(() -> client.sendPacket(new PacketOutgoingHandshakeRequest(
        node.hostname(), node.distributionPort(), node.distributionKey())));
    }
  }

  private static void registerCommands(
    CommandRegistry registry, Injector injector
  ) {
    registry.register(injector.getInstance(HelpCommand.class));
    registry.register(injector.getInstance(ModuleCommand.class));
    registry.register(injector.getInstance(DistributionCommand.class));
    registry.register(injector.getInstance(TemplateCommand.class));
    registry.register(injector.getInstance(UserCommand.class));
    registry.register(injector.getInstance(ExitCommand.class));
  }
}