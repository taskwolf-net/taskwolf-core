package de.flexpedite.access.security;

import com.google.inject.Inject;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class FilterConfiguration {
  private final AuthorizationFilter authorizationFilter;

  @Bean
  public FilterRegistrationBean<AuthorizationFilter> registrationBean() {
    var registrationBean = new FilterRegistrationBean<AuthorizationFilter>();
    registrationBean.setFilter(authorizationFilter);
    return registrationBean;
  }
}
