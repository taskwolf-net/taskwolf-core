package com.dulno.core;

import com.dulno.core.access.AccessInjectionModule;
import com.dulno.core.action.ActionInjectionModule;
import com.dulno.core.bundle.BundleInjectionModule;
import com.dulno.core.command.CommandInjectionModule;
import com.dulno.core.condition.ConditionInjectionModule;
import com.dulno.core.database.DatabaseConnection;
import com.dulno.core.database.DatabaseInjectionModule;
import com.dulno.core.database.DatabaseKeyspace;
import com.dulno.core.error.ErrorInjectionModule;
import com.dulno.core.intro.IntroInjectionModule;
import com.dulno.core.locale.LocaleInjectionModule;
import com.dulno.core.log.Log;
import com.dulno.core.mail.MailInjectionModule;
import com.dulno.core.maintenance.MaintenanceInjectionModule;
import com.dulno.core.notification.NotificationInjectionModule;
import com.dulno.core.offer.OfferInjectionModule;
import com.dulno.core.organization.OrganizationInjectionModule;
import com.dulno.core.question.QuestionInjectionModule;
import com.dulno.core.recaptcha.RecaptchaInjectionModule;
import com.dulno.core.sale.SaleInjectionModule;
import com.dulno.core.session.SessionInjectionModule;
import com.dulno.core.stripe.StripeInjectionModule;
import com.dulno.core.template.TemplateInjectionModule;
import com.dulno.core.ticket.TicketInjectionModule;
import com.dulno.core.trial.TrialInjectionModule;
import com.dulno.core.trigger.TriggerInjectionModule;
import com.dulno.core.tutorial.TutorialInjectionModule;
import com.dulno.core.user.UserInjectionModule;
import com.dulno.core.whitelist.WhitelistInjectionModule;
import com.dulno.core.worker.WorkerInjectionModule;
import com.dulno.core.workflow.WorkflowInjectionModule;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import com.dulno.core.module.ModuleInjectionModule;
import com.dulno.core.module.ModuleLoader;
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
    install(SessionInjectionModule.create());
    install(MailInjectionModule.create());
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
    install(OfferInjectionModule.create());
    install(MaintenanceInjectionModule.create());
    install(ErrorInjectionModule.create());
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
