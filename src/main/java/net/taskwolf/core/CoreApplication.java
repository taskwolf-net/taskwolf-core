package net.taskwolf.core;

import net.taskwolf.core.command.implementation.*;
import net.taskwolf.core.database.DatabaseConnection;
import net.taskwolf.core.error.ErrorRepository;
import net.taskwolf.core.event.EventExecutor;
import net.taskwolf.core.event.HookRegistry;
import net.taskwolf.core.intro.Intro;
import net.taskwolf.core.module.ModuleLoader;
import net.taskwolf.core.tutorial.level.bundle.BundleTutorialLevel;
import net.taskwolf.core.tutorial.level.organization.OrganizationMembersTutorialLevel;
import net.taskwolf.core.tutorial.level.organization.OrganizationTeamsTutorialLevel;
import net.taskwolf.core.worker.WorkerConfiguration;
import net.taskwolf.core.worker.client.WorkerProxyClient;
import com.google.inject.Guice;
import com.google.inject.Injector;
import net.taskwolf.core.application.CoreApplicationLaunchEvent;
import net.taskwolf.core.application.CoreApplicationPostRunEvent;
import net.taskwolf.core.application.CoreApplicationPreRunEvent;
import net.taskwolf.core.command.CommandRegistry;
import net.taskwolf.core.command.CommandTask;
import net.taskwolf.core.database.transformation.DatabaseDiscrepancyHook;
import net.taskwolf.core.log.Log;
import net.taskwolf.core.maintenance.MaintenanceSchedule;
import net.taskwolf.core.tutorial.level.TutorialLevelRegistry;
import net.taskwolf.core.tutorial.level.account.AccountsTutorialLevel;
import net.taskwolf.core.tutorial.level.dashboard.DashboardTutorialLevel;
import net.taskwolf.core.tutorial.level.database.DatabasesTutorialLevel;
import net.taskwolf.core.tutorial.level.device.DevicesTutorialLevel;
import net.taskwolf.core.tutorial.level.help.HelpTutorialLevel;
import net.taskwolf.core.tutorial.level.process.ProcessTutorialLevel;
import net.taskwolf.core.tutorial.level.process.ProcessesTutorialLevel;
import net.taskwolf.core.tutorial.level.template.TemplateTutorialLevel;
import net.taskwolf.core.tutorial.level.webhook.WebhooksTutorialLevel;
import net.taskwolf.core.tutorial.level.workflow.WorkflowTutorialLevel;
import net.taskwolf.core.tutorial.level.workflow.WorkflowsTutorialLevel;
import net.taskwolf.core.worker.WorkerDistribution;
import net.taskwolf.core.worker.packet.outgoing.node.PacketOutgoingDisconnect;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Collections;

@SpringBootApplication(scanBasePackages = {"net.taskwolf"},
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
      log.info("Initializing Taskwolf - Core");
      registerHooks(injector.getInstance(HookRegistry.class), injector);
      var eventExecutor = injector.getInstance(EventExecutor.class);
      eventExecutor.execute(CoreApplicationLaunchEvent.create());
      var distributionConfiguration = injector.getInstance(WorkerConfiguration.class);
      var distribution = injector.getInstance(WorkerDistribution.class);
      distribution.initialize();
      injector.getInstance(ModuleLoader.class).loadModules();
      registerTutorialLevels(injector.getInstance(TutorialLevelRegistry.class));
      var commandRegistry = injector.getInstance(CommandRegistry.class);
      registerCommands(commandRegistry, injector);
      var application = injector.getInstance(SpringApplication.class);
      application.setDefaultProperties(Collections.singletonMap("server.port",
        distributionConfiguration.restPort()));
      eventExecutor.execute(CoreApplicationPreRunEvent.create());
      log.info("Booting Spring...");
      application.run(args);
      new Thread(() -> CommandTask.create(log, errorRepository, commandRegistry)
        .start()).start();
      injector.getInstance(MaintenanceSchedule.class).start();
      log.info("Successfully booted Taskwolf - Core");
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
    registry.register(injector.getInstance(ExitCommand.class));
  }
}