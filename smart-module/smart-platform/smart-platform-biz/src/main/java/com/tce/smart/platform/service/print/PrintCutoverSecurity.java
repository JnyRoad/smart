package com.tce.smart.platform.service.print;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

/** 仅开放一个只读开关入口；不能将整个打印域或设备执行接口加入匿名白名单。 */
@Configuration @Order(-101)
public class PrintCutoverSecurity extends WebSecurityConfigurerAdapter {
    @Override protected void configure(HttpSecurity http)throws Exception {
        http.requestMatcher(new AntPathRequestMatcher("/print/v1/cutover","GET"))
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
            .authorizeRequests().anyRequest().permitAll().and().csrf().disable();
    }
}
