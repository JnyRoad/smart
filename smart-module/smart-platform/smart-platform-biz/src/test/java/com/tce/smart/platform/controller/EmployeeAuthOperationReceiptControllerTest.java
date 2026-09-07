package com.tce.smart.platform.controller;

import com.tce.smart.common.core.config.GlobalExceptionHandlerResolver;
import com.tce.smart.platform.service.SmtDeviceAuthorityService;
import org.junit.*;
import org.mockito.*;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** 新回执校验不能被全局异常处理降为 HTTP 200。 */
public class EmployeeAuthOperationReceiptControllerTest {
 private MockMvc mvc;
 private SmtDeviceAuthorityService service;
 @Before public void setup() throws Exception {
  service=Mockito.mock(SmtDeviceAuthorityService.class);
  try {
   Object controller=Class.forName("com.tce.smart.platform.controller.EmployeeAuthOperationReceiptController").getConstructor(SmtDeviceAuthorityService.class).newInstance(service);
   mvc=MockMvcBuilders.standaloneSetup(controller).setControllerAdvice(new GlobalExceptionHandlerResolver()).build();
  } catch(ClassNotFoundException missing) {Assert.fail("独立人员回执Controller尚未实现");}
 }
 @Test public void emptySelectionIsHttp400DespiteGlobalAdvice() throws Exception {
  mvc.perform(post("/device/authority/relation/person/del/receipt").contentType(MediaType.APPLICATION_JSON)
   .content("{\"authId\":9,\"type\":2,\"delIds\":[]}")).andExpect(status().isBadRequest());
 }
 @Test public void missingSelectionFieldsAreHttp400() throws Exception {
  mvc.perform(post("/device/authority/relation/person/del/receipt").contentType(MediaType.APPLICATION_JSON)
   .content("{}")).andExpect(status().isBadRequest());
 }
 @Test public void unauthenticatedClearIsHttp403() throws Exception {
  mvc.perform(post("/device/authority/relation/person/clear/9/receipt")).andExpect(status().isForbidden());
 }
 @After public void clearSecurityContext() {org.springframework.security.core.context.SecurityContextHolder.clearContext();}
 private void signIn() {
  com.tce.smart.common.security.service.SmartUser user=new com.tce.smart.common.security.service.SmartUser(1,1,"tester",java.util.Collections.singletonList(1),"unused",true,true,true,true,java.util.Collections.emptyList());
  org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(user,null,java.util.Collections.emptyList()));
 }
 @Test public void sourceConflictIsHttp409AndNotGlobal200() throws Exception {
  signIn();Mockito.when(service.personRelationClearReceipt(Mockito.eq(9),Mockito.eq(java.util.Collections.singletonList(1)))).thenThrow(new IllegalStateException("PENDING_SELECTION"));
  mvc.perform(post("/device/authority/relation/person/clear/9/receipt")).andExpect(status().isConflict());
 }
 @Test public void actualAcceptedKeySurvivesHttpSerialization() throws Exception {
  signIn();Mockito.when(service.personRelationClearReceipt(Mockito.eq(9),Mockito.eq(java.util.Collections.singletonList(1)))).thenReturn(com.tce.smart.platform.dto.authoperation.AuthOperationReceipt.reliable("actual-operation-key"));
  mvc.perform(post("/device/authority/relation/person/clear/9/receipt")).andExpect(status().isOk())
   .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.data.operationKey").value("actual-operation-key"))
   .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.data.mode").value("RELIABLE"));
 }
 @Test public void oldDeleteRouteStillReturnsBoolean() throws Exception {
  Mockito.when(service.deviceAuthRelationDel(Mockito.any())).thenReturn(true);
  MockMvc old=MockMvcBuilders.standaloneSetup(new SmtDeviceAuthorityController(service)).build();
  old.perform(post("/device/authority/relation/del").contentType(MediaType.APPLICATION_JSON).content("{\"authId\":9,\"type\":1,\"delIds\":[5]}"))
   .andExpect(status().isOk()).andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.data").value(true));
 }
 @Test public void unexpectedFailureLogsThrowableWithoutExposingRequestOrInternalError() throws Exception {
  signIn();
  RuntimeException failure=new RuntimeException("internal-diagnostic-marker");
  Mockito.when(service.personRelationClearReceipt(Mockito.eq(9),Mockito.eq(java.util.Collections.singletonList(1)))).thenThrow(failure);
  ch.qos.logback.classic.Logger logger=(ch.qos.logback.classic.Logger)org.slf4j.LoggerFactory.getLogger(EmployeeAuthOperationReceiptController.class);
  ch.qos.logback.classic.Level oldLevel=logger.getLevel();boolean oldAdditive=logger.isAdditive();
  ch.qos.logback.core.read.ListAppender<ch.qos.logback.classic.spi.ILoggingEvent> appender=new ch.qos.logback.core.read.ListAppender<>();
  appender.start();logger.addAppender(appender);logger.setLevel(ch.qos.logback.classic.Level.INFO);logger.setAdditive(false);
  try {
   String response=mvc.perform(post("/device/authority/relation/person/clear/9/receipt")
     .contentType(MediaType.APPLICATION_JSON).content("{\"requestMarker\":\"request-body-only-marker\"}"))
    .andExpect(status().isInternalServerError()).andReturn().getResponse().getContentAsString();
   Assert.assertFalse(response.contains("internal-diagnostic-marker"));
   Assert.assertFalse(response.contains("request-body-only-marker"));
   Assert.assertEquals("非预期500必须记录且只记录一条可诊断异常",1,appender.list.size());
   ch.qos.logback.classic.spi.ILoggingEvent event=appender.list.get(0);
   Assert.assertEquals(ch.qos.logback.classic.Level.ERROR,event.getLevel());
   Assert.assertNotNull(event.getThrowableProxy());
   Assert.assertEquals(RuntimeException.class.getName(),event.getThrowableProxy().getClassName());
   Assert.assertEquals("internal-diagnostic-marker",event.getThrowableProxy().getMessage());
   Assert.assertTrue(event.getThrowableProxy().getStackTraceElementProxyArray().length>0);
   Assert.assertFalse(event.getFormattedMessage().contains("request-body-only-marker"));
   Assert.assertTrue(event.getArgumentArray()==null || event.getArgumentArray().length==0);
  } finally {logger.detachAppender(appender);logger.setLevel(oldLevel);logger.setAdditive(oldAdditive);appender.stop();}
 }
}
