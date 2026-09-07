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
public class EmployeeAuthOperationIntakeControllerTest {
 private MockMvc mvc;
 private SmtDeviceAuthorityService service;
 @Before public void setup() throws Exception {
  service=Mockito.mock(SmtDeviceAuthorityService.class);
  try {
   Object controller=Class.forName("com.tce.smart.platform.controller.EmployeeAuthOperationReceiptController").getConstructor(SmtDeviceAuthorityService.class).newInstance(service);
   mvc=MockMvcBuilders.standaloneSetup(controller).setControllerAdvice(new GlobalExceptionHandlerResolver()).build();
  } catch(ClassNotFoundException missing) {Assert.fail("独立人员回执Controller尚未实现");}
 }
 @After public void clearSecurityContext() {org.springframework.security.core.context.SecurityContextHolder.clearContext();}
 private void signIn() {
  com.tce.smart.common.security.service.SmartUser user=new com.tce.smart.common.security.service.SmartUser(1,1,"tester",java.util.Collections.singletonList(1),"unused",true,true,true,true,java.util.Collections.emptyList());
  org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(user,null,java.util.Collections.emptyList()));
 }
 @Test public void requestKeyUsesAuthenticatedActorAndPreservesStringBatchIds() throws Exception {
  signIn();Mockito.when(service.personRelationClearIntake(9,"original-client-key",1,java.util.Collections.singletonList(1)))
   .thenReturn(com.tce.smart.platform.dto.authoperation.AuthOperationIntakeReceipt.builder().requestKey("original-client-key").mode("RELIABLE").submitted(true).operationKey("assigned-operation").batch("9007199254740993",1).build());
  mvc.perform(post("/device/authority/relation/person/clear/9/receipt").header("Idempotency-Key","original-client-key"))
   .andExpect(status().isOk()).andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.data.requestKey").value("original-client-key"))
   .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.data.batchParks['9007199254740993']").value(1));
  Mockito.verify(service,Mockito.never()).personRelationClearReceipt(Mockito.anyInt(),Mockito.anyList());
 }
 @Test public void headerDeleteCallsKeyedPathOnly() throws Exception {
  signIn();mvc.perform(post("/device/authority/relation/person/del/receipt").header("Idempotency-Key","original-client-key").contentType(MediaType.APPLICATION_JSON)
   .content("{\"authId\":9,\"type\":1,\"delIds\":[5],\"actorId\":99}")).andExpect(status().isOk());
  Mockito.verify(service).personRelationDeleteIntake(Mockito.any(),Mockito.eq("original-client-key"),Mockito.eq(1),Mockito.eq(java.util.Collections.singletonList(1)));
  Mockito.verify(service,Mockito.never()).personRelationDeleteReceipt(Mockito.any(),Mockito.anyList());
 }
 @Test public void capabilityReturnsOnlyVersionAndEnabledAndNeedsLogin() throws Exception {
  mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/device/authority/relation/person/9/intake-capability")).andExpect(status().isForbidden());
  signIn();Mockito.when(service.personIntakeCapability(9,java.util.Collections.singletonList(1))).thenReturn(new com.tce.smart.platform.dto.authoperation.AuthOperationIntakeCapability(1,true));
  mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/device/authority/relation/person/9/intake-capability")).andExpect(status().isOk())
   .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.data.intakeVersion").value(1))
   .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.data.reliableIntakeEnabled").value(true));
 }
 @Test public void keyedUnsupportedIs409AndNeverLegacy() throws Exception {
  signIn();Mockito.when(service.personRelationClearIntake(9,"original-client-key",1,java.util.Collections.singletonList(1)))
   .thenThrow(new com.tce.smart.platform.service.impl.EmployeeAuthIntakeService.IntakeException("KEYED_UNSUPPORTED"));
  mvc.perform(post("/device/authority/relation/person/clear/9/receipt").header("Idempotency-Key","original-client-key")).andExpect(status().isConflict())
   .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.msg").value(org.hamcrest.Matchers.startsWith("KEYED_UNSUPPORTED：")));
  Mockito.verify(service,Mockito.never()).personRelationClearReceipt(Mockito.anyInt(),Mockito.anyList());
 }
}
