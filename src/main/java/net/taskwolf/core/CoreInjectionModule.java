package net.taskwolf.core;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.taskwolf.core.action.ActionInjectionModule;
import net.taskwolf.core.command.CommandInjectionModule;
import net.taskwolf.core.condition.ConditionInjectionModule;
import net.taskwolf.core.database.DatabaseInjectionModule;
import net.taskwolf.core.distribution.DistributionInjectionModule;
import net.taskwolf.core.locale.LocaleInjectionModule;
import net.taskwolf.core.log.Log;
import net.taskwolf.core.module.ModuleInjectionModule;
import net.taskwolf.core.module.ModuleLoader;
import net.taskwolf.core.notification.NotificationInjectionModule;
import net.taskwolf.core.organization.OrganizationInjectionModule;
import net.taskwolf.core.template.TemplateInjectionModule;
import net.taskwolf.core.ticket.TicketInjectionModule;
import net.taskwolf.core.trigger.TriggerInjectionModule;
import net.taskwolf.core.user.UserInjectionModule;
import net.taskwolf.core.workflow.WorkflowInjectionModule;
import org.springframework.boot.SpringApplication;
import org.springframework.core.io.DefaultResourceLoader;

@RequiredArgsConstructor(staticName = "create")
public final class CoreInjectionModule extends AbstractModule {
  @Override
  protected void configure() {
    configureLog();
    install(DatabaseInjectionModule.create());
    install(UserInjectionModule.create());
    install(OrganizationInjectionModule.create());
    install(TriggerInjectionModule.create());
    install(ActionInjectionModule.create());
    install(ConditionInjectionModule.create());
    install(WorkflowInjectionModule.create());
    install(TemplateInjectionModule.create());
    install(TicketInjectionModule.create());
    install(NotificationInjectionModule.create());
    install(DistributionInjectionModule.create());
    install(LocaleInjectionModule.create());
    install(ModuleInjectionModule.create());
    install(CommandInjectionModule.create());
  }

  private void configureLog() {
    try {
      bind(Log.class).toInstance(Log.create("Core", "/logs/"));
    } catch (Exception exception) {
      exception.printStackTrace();
    }
  }

  @Provides
  @Singleton
  SpringApplication provideSpringApplication(ModuleLoader moduleLoader) {
    var application = new SpringApplication(CoreApplication.class);
    var classLoader = moduleLoader.createModuleClassLoader();
    application.setResourceLoader(new DefaultResourceLoader(classLoader));
    return application;
  }
}
