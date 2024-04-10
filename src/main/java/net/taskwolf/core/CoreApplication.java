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
import net.taskwolf.core.distribution.Distribution;
import net.taskwolf.core.distribution.DistributionConfiguration;
import net.taskwolf.core.distribution.server.node.NodePingSchedule;
import net.taskwolf.core.intro.Intro;
import net.taskwolf.core.log.Log;
import net.taskwolf.core.module.ModuleLoader;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Collections;

@SpringBootApplication(scanBasePackages = {"net.taskwolf"},
  exclude = {org.springframework.boot.autoconfigure.gson.GsonAutoConfiguration.class})
public class CoreApplication {
  public static void main(String[] args) throws Exception {
    var injector = Guice.createInjector(CoreInjectionModule.create());
    System.out.print("\033c");
    injector.getInstance(Intro.class).print();
    var log = injector.getInstance(Log.class);
    log.info("Initializing Taskwolf - Core");
    var application = injector.getInstance(SpringApplication.class);
    registerConditions(injector.getInstance(ConditionInformationRepository.class));
    var distributionConfiguration = injector.getInstance(
      DistributionConfiguration.class);
    setupDistribution(injector);
    var coreModule = injector.getInstance(CoreModule.class);
    coreModule.initialize();
    var commandRegistry = injector.getInstance(CommandRegistry.class);
    registerCommands(commandRegistry, injector);
    application.setDefaultProperties(Collections.singletonMap("server.port",
      distributionConfiguration.self().restPort()));
    application.run(args);
    new Thread(() -> CommandTask.create(log, commandRegistry).start()).start();
    log.info("Successfully booted Taskwolf - Core");
  }

  private static void setupDistribution(Injector injector) throws Exception {
    var distribution = injector.getInstance(Distribution.class);
    distribution.initialize();
    var nodePingScheduler = injector.getInstance(NodePingSchedule.class);
    nodePingScheduler.start();
  }

  private static void registerConditions(ConditionInformationRepository repository) {
    repository.register(ConditionTextEquals.information());
    repository.register(ConditionTextStartsWith.information());
    repository.register(ConditionTextEndsWith.information());
    repository.register(ConditionNumberGreaterThan.information());
    repository.register(ConditionNumberSmallerThan.information());
  }

  private static void registerCommands(
    CommandRegistry registry, Injector injector
  ) {
    registry.register(injector.getInstance(ClearCommand.class));
    registry.register(injector.getInstance(HelpCommand.class));
    registry.register(injector.getInstance(DistributionCommand.class));
    registry.register(injector.getInstance(TemplateCommand.class));
    registry.register(injector.getInstance(UserCommand.class));
    registry.register(injector.getInstance(ExitCommand.class));
  }
}