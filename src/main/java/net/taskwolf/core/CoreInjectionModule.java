package net.taskwolf.core;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import net.taskwolf.core.access.AccessInjectionModule;
import net.taskwolf.core.action.ActionInjectionModule;
import net.taskwolf.core.bundle.BundleInjectionModule;
import net.taskwolf.core.command.CommandInjectionModule;
import net.taskwolf.core.condition.ConditionInjectionModule;
import net.taskwolf.core.database.DatabaseInjectionModule;
import net.taskwolf.core.intro.IntroInjectionModule;
import net.taskwolf.core.locale.LocaleInjectionModule;
import net.taskwolf.core.log.Log;
import net.taskwolf.core.module.ModuleInjectionModule;
import net.taskwolf.core.module.ModuleLoader;
import net.taskwolf.core.notification.NotificationInjectionModule;
import net.taskwolf.core.organization.OrganizationInjectionModule;
import net.taskwolf.core.question.QuestionInjectionModule;
import net.taskwolf.core.recaptcha.RecaptchaInjectionModule;
import net.taskwolf.core.sale.SaleInjectionModule;
import net.taskwolf.core.stripe.StripeInjectionModule;
import net.taskwolf.core.template.TemplateInjectionModule;
import net.taskwolf.core.ticket.TicketInjectionModule;
import net.taskwolf.core.trial.TrialInjectionModule;
import net.taskwolf.core.trigger.TriggerInjectionModule;
import net.taskwolf.core.tutorial.TutorialInjectionModule;
import net.taskwolf.core.user.UserInjectionModule;
import net.taskwolf.core.whitelist.WhitelistInjectionModule;
import net.taskwolf.core.worker.WorkerInjectionModule;
import net.taskwolf.core.workflow.WorkflowInjectionModule;
import org.springframework.boot.SpringApplication;
import org.springframework.core.io.DefaultResourceLoader;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class CoreInjectionModule extends AbstractModule {
  public static CoreInjectionModule create() {
    return new CoreInjectionModule();
  }

  @Override
  protected void configure() {
    install(IntroInjectionModule.create());
    install(DatabaseInjectionModule.create());
    install(UserInjectionModule.create());
    install(OrganizationInjectionModule.create());
    install(BundleInjectionModule.create());
    install(TriggerInjectionModule.create());
    install(ActionInjectionModule.create());
    install(ConditionInjectionModule.create());
    install(WorkflowInjectionModule.create());
    install(TemplateInjectionModule.create());
    install(TicketInjectionModule.create());
    install(QuestionInjectionModule.create());
    install(SaleInjectionModule.create());
    install(NotificationInjectionModule.create());
    install(WorkerInjectionModule.create());
    install(WhitelistInjectionModule.create());
    install(RecaptchaInjectionModule.create());
    install(LocaleInjectionModule.create());
    install(TutorialInjectionModule.create());
    install(AccessInjectionModule.create());
    install(ModuleInjectionModule.create());
    install(CommandInjectionModule.create());
    install(StripeInjectionModule.create());
    install(TrialInjectionModule.create());
  }

  @Provides
  @Singleton
  Log provideCoreLog() throws Exception {
    return Log.create("Core", "/logs/");
  }

  @Provides
  @Singleton
  SpringApplication provideSpringApplication(ModuleLoader moduleLoader) {
    var application = new SpringApplication(CoreApplication.class);
    var classLoader = moduleLoader.classLoader();
    application.setResourceLoader(new DefaultResourceLoader(classLoader));
    return application;
  }
}
