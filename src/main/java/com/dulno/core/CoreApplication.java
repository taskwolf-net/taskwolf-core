package com.dulno.core;

import com.dulno.core.command.implementation.*;
import com.dulno.core.condition.ConditionInformationRepository;
import com.dulno.core.database.DatabaseConnection;
import com.dulno.core.error.ErrorRepository;
import com.dulno.core.event.EventExecutor;
import com.dulno.core.event.HookRegistry;
import com.dulno.core.intro.Intro;
import com.dulno.core.loop.LoopInformationRepository;
import com.dulno.core.loop.type.NumberLoop;
import com.dulno.core.tutorial.level.bundle.BundleTutorialLevel;
import com.dulno.core.tutorial.level.organization.OrganizationMembersTutorialLevel;
import com.dulno.core.tutorial.level.organization.OrganizationTeamsTutorialLevel;
import com.dulno.core.worker.WorkerConfiguration;
import com.dulno.core.worker.client.WorkerProxyClient;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.dulno.core.application.CoreApplicationLaunchEvent;
import com.dulno.core.application.CoreApplicationPostRunEvent;
import com.dulno.core.application.CoreApplicationPreRunEvent;
import com.dulno.core.command.CommandRegistry;
import com.dulno.core.command.CommandTask;
import com.dulno.core.condition.number.ConditionNumberGreaterThan;
import com.dulno.core.condition.number.ConditionNumberSmallerThan;
import com.dulno.core.condition.text.ConditionTextEndsWith;
import com.dulno.core.condition.text.ConditionTextEquals;
import com.dulno.core.condition.text.ConditionTextStartsWith;
import com.dulno.core.database.transformation.DatabaseDiscrepancyHook;
import com.dulno.core.log.Log;
import com.dulno.core.maintenance.MaintenanceSchedule;
import com.dulno.core.tutorial.level.TutorialLevelRegistry;
import com.dulno.core.tutorial.level.account.AccountsTutorialLevel;
import com.dulno.core.tutorial.level.dashboard.DashboardTutorialLevel;
import com.dulno.core.tutorial.level.database.DatabasesTutorialLevel;
import com.dulno.core.tutorial.level.device.DevicesTutorialLevel;
import com.dulno.core.tutorial.level.help.HelpTutorialLevel;
import com.dulno.core.tutorial.level.process.ProcessTutorialLevel;
import com.dulno.core.tutorial.level.process.ProcessesTutorialLevel;
import com.dulno.core.tutorial.level.template.TemplateTutorialLevel;
import com.dulno.core.tutorial.level.webhook.WebhooksTutorialLevel;
import com.dulno.core.tutorial.level.workflow.WorkflowTutorialLevel;
import com.dulno.core.tutorial.level.workflow.WorkflowsTutorialLevel;
import com.dulno.core.worker.WorkerDistribution;
import com.dulno.core.worker.packet.outgoing.node.PacketOutgoingDisconnect;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Collections;

@SpringBootApplication(scanBasePackages = {"com.dulno"},
  exclude = {org.springframework.boot.autoconfigure.gson.GsonAutoConfiguration.class})
public class CoreApplication {
  /**
   * The starting point where the application is executed
   * @param args The arguments that are passed into the application
   */
  public static void main(String[] args) {
    var injector = Guice.createInjector(CoreInjectionModule.create());
    var errorRepository = injector.getInstance(ErrorRepository.class);
    Thread.setDefaultUncaughtExceptionHandler((thread, throwable) ->
      errorRepository.processError(throwable));
    injector.getInstance(DatabaseConnection.class).errorRepository(errorRepository);
    try {
      injector.getInstance(Intro.class).print();
      var log = injector.getInstance(Log.class);
      log.info("Initializing Dulno - Core");
      registerHooks(injector.getInstance(HookRegistry.class), injector);
      var eventExecutor = injector.getInstance(EventExecutor.class);
      eventExecutor.execute(CoreApplicationLaunchEvent.create());
      registerConditions(injector.getInstance(ConditionInformationRepository.class));
      registerLoops(injector.getInstance(LoopInformationRepository.class));
      registerTutorialLevels(injector.getInstance(TutorialLevelRegistry.class));
      var distributionConfiguration = injector.getInstance(WorkerConfiguration.class);
      var distribution = injector.getInstance(WorkerDistribution.class);
      distribution.initialize();
      var coreModule = injector.getInstance(CoreModule.class);
      coreModule.initialize();
      var commandRegistry = injector.getInstance(CommandRegistry.class);
      registerCommands(commandRegistry, injector);
      var application = injector.getInstance(SpringApplication.class);
      application.setDefaultProperties(Collections.singletonMap("server.port",
        distributionConfiguration.restPort()));
      eventExecutor.execute(CoreApplicationPreRunEvent.create());
      application.run(args);
      new Thread(() -> CommandTask.create(log, errorRepository, commandRegistry)
        .start()).start();
      injector.getInstance(MaintenanceSchedule.class).start();
      log.info("Successfully booted Dulno - Core");
      Runtime.getRuntime().addShutdownHook(new Thread(() ->
        injector.getInstance(WorkerProxyClient.class)
          .sendPacket(new PacketOutgoingDisconnect())));
      eventExecutor.execute(CoreApplicationPostRunEvent.create());
    } catch (Exception exception) {
      errorRepository.processError(exception);
    }
  }

  private static void registerHooks(HookRegistry registry, Injector injector) {
    registry.register(injector.getInstance(DatabaseDiscrepancyHook.class));
  }

  private static void registerConditions(ConditionInformationRepository repository) {
    repository.register(ConditionTextEquals.information());
    repository.register(ConditionTextStartsWith.information());
    repository.register(ConditionTextEndsWith.information());
    repository.register(ConditionNumberGreaterThan.information());
    repository.register(ConditionNumberSmallerThan.information());
  }

  private static void registerLoops(LoopInformationRepository repository) {
    repository.register(NumberLoop.information());
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
    registry.registerLevel(DevicesTutorialLevel.create());
    registry.registerLevel(AccountsTutorialLevel.create());
    registry.registerLevel(BundleTutorialLevel.create());
    registry.registerLevel(OrganizationMembersTutorialLevel.create());
    registry.registerLevel(OrganizationTeamsTutorialLevel.create());
    registry.registerLevel(HelpTutorialLevel.create());
  }

  private static void registerCommands(
    CommandRegistry registry, Injector injector
  ) {
    registry.register(injector.getInstance(ClearCommand.class));
    registry.register(injector.getInstance(HelpCommand.class));
    registry.register(injector.getInstance(UserCommand.class));
    registry.register(injector.getInstance(BundleCommand.class));
    registry.register(injector.getInstance(ExitCommand.class));
  }
}