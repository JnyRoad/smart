package com.tce.smart.platform.service.print;
import com.tce.smart.platform.controller.print.PrintApiAdvice;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;
import javax.servlet.*;
import javax.servlet.http.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
/** 仅设备API使用此独立安全链，既有管理员会话永远不能替代设备凭据。 */
@Configuration @Order(-100)
public class PrintClientSecurity extends WebSecurityConfigurerAdapter {
 private final PrintClientProperties properties;
 public PrintClientSecurity(PrintClientProperties properties){this.properties=properties;}
 @Override protected void configure(HttpSecurity http)throws Exception{http.antMatcher("/api/print-client/v1/**").csrf().disable().requestCache().disable().sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and().addFilterBefore(new DeviceFilter(properties),UsernamePasswordAuthenticationFilter.class).authorizeRequests().anyRequest().authenticated();}
 public static final class DeviceFilter extends OncePerRequestFilter {
  final PrintClientProperties properties;public DeviceFilter(PrintClientProperties properties){this.properties=properties;}
  @Override protected void doFilterInternal(HttpServletRequest request,HttpServletResponse response,FilterChain chain)throws ServletException,IOException{
   String requestId=UUID.randomUUID().toString();request.setAttribute("print.requestId",requestId);response.setHeader("X-Request-Id",requestId);response.setHeader("Cache-Control","no-store");SecurityContextHolder.clearContext();
   try{PrintClientIdentity identity=authenticate(properties,request.getHeader("Authorization"));SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(identity,null,Collections.emptyList()));chain.doFilter(request,response);}
   catch(PrintApiException failure){response.setStatus(failure.getStatus());response.setContentType("application/json");response.getWriter().write(PrintJson.canonical(PrintApiAdvice.error(failure,requestId).getBody()));}
   finally{SecurityContextHolder.clearContext();}
  }
 }
 public static PrintClientIdentity authenticate(PrintClientProperties properties,String header){if(header==null||!header.startsWith("Bearer ")||header.length()<23||header.length()>1024)throw PrintJobTransactions.error(401,"PRINT_DEVICE_AUTHENTICATION_REQUIRED");String hash=PrintJson.hashBytes(header.substring(7).getBytes(StandardCharsets.UTF_8));PrintClientProperties.Credential found=null;for(PrintClientProperties.Credential c:properties.getCredentials()){if(c.getTokenSha256()!=null&&c.getTokenSha256().matches("sha256:[a-f0-9]{64}")&&MessageDigest.isEqual(hash.getBytes(StandardCharsets.US_ASCII),c.getTokenSha256().getBytes(StandardCharsets.US_ASCII))){if(found!=null)throw PrintJobTransactions.error(401,"PRINT_DEVICE_AUTHENTICATION_REQUIRED");found=c;}}if(found==null||found.getDeviceIdentity()==null||!found.getDeviceIdentity().matches("[\\x21-\\x7e]{1,128}")||found.getParkIds().isEmpty()||found.getPrinterProfileIds().isEmpty())throw PrintJobTransactions.error(401,"PRINT_DEVICE_AUTHENTICATION_REQUIRED");return new PrintClientIdentity(found.getDeviceIdentity(),found.getParkIds(),found.getPrinterProfileIds());}
}
