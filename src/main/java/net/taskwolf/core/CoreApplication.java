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
import net.taskwolf.core.intro.Intro;
import net.taskwolf.core.log.Log;
import net.taskwolf.core.tutorial.level.TutorialLevelRegistry;
import net.taskwolf.core.tutorial.level.account.AccountsTutorialLevel;
import net.taskwolf.core.tutorial.level.dashboard.*;
import net.taskwolf.core.tutorial.level.database.DatabasesTutorialLevel;
import net.taskwolf.core.tutorial.level.device.DevicesTutorialLevel;
import net.taskwolf.core.tutorial.level.help.HelpTutorialLevel;
import net.taskwolf.core.tutorial.level.organization.OrganizationsTutorialLevel;
import net.taskwolf.core.tutorial.level.process.ProcessTutorialLevel;
import net.taskwolf.core.tutorial.level.process.ProcessesTutorialLevel;
import net.taskwolf.core.tutorial.level.template.TemplateTutorialLevel;
import net.taskwolf.core.tutorial.level.webhook.WebhooksTutorialLevel;
import net.taskwolf.core.tutorial.level.workflow.WorkflowTutorialLevel;
import net.taskwolf.core.tutorial.level.workflow.WorkflowsTutorialLevel;
import net.taskwolf.core.worker.WorkerConfiguration;
import net.taskwolf.core.worker.WorkerDistribution;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Collections;

@SpringBootApplication(scanBasePackages = {"net.taskwolf"},
  exclude = {org.springframework.boot.autoconfigure.gson.GsonAutoConfiguration.class})
public class CoreApplication {
  /**
   * The starting point where the application is executed
   * @param args The arguments that are passed into the application
   * @throws Exception
   */
  public static void main(String[] args) throws Exception {
    var injector = Guice.createInjector(CoreInjectionModule.create());
    injector.getInstance(Intro.class).print();
    var log = injector.getInstance(Log.class);
    log.info("Initializing Taskwolf - Core");
    var application = injector.getInstance(SpringApplication.class);
    registerConditions(injector.getInstance(ConditionInformationRepository.class));
    registerTutorialLevels(injector.getInstance(TutorialLevelRegistry.class));
    var distributionConfiguration = injector.getInstance(WorkerConfiguration.class);
    var distribution = injector.getInstance(WorkerDistribution.class);
    distribution.initialize();
    var coreModule = injector.getInstance(CoreModule.class);
    coreModule.initialize();
    var commandRegistry = injector.getInstance(CommandRegistry.class);
    registerCommands(commandRegistry, injector);
    application.setDefaultProperties(Collections.singletonMap("server.port",
      distributionConfiguration.restPort()));
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

  private static void registerTutorialLevels(TutorialLevelRegistry registry) {
    registry.registerLevel(DashboardTutorialLevel.create());
    registry.registerLevel(WorkflowsTutorialLevel.create());
    registry.registerLevel(WorkflowTutorialLevel.create());
    registry.registerLevel(TemplateTutorialLevel.create());
    registry.registerLevel(ProcessesTutorialLevel.create());
    registry.registerLevel(ProcessTutorialLevel.create());
    registry.registerLevel(DatabasesTutorialLevel.create());
    registry.registerLevel(WebhooksTutorialLevel.create());
    registry.registerLevel(OrganizationsTutorialLevel.create());
    registry.registerLevel(AccountsTutorialLevel.create());
    registry.registerLevel(DevicesTutorialLevel.create());
    registry.registerLevel(HelpTutorialLevel.create());
  }

  private static void registerCommands(
    CommandRegistry registry, Injector injector
  ) {
    registry.register(injector.getInstance(ClearCommand.class));
    registry.register(injector.getInstance(HelpCommand.class));
    registry.register(injector.getInstance(TemplateCommand.class));
    registry.register(injector.getInstance(UserCommand.class));
    registry.register(injector.getInstance(ExitCommand.class));
  }
}