package com.tce.smart.platform.core.service.impl;
import com.tce.smart.platform.core.entity.SmtAuthTransportPhase;
import org.junit.Test;
import static org.junit.Assert.*;
/** 请求未知和分组越界均不能产生额外设备命令。 */
public class AuthOperationTransportTest {
 @Test public void unknownAndIntentNeverBecomeSendableAgain() {
  SmtAuthTransportPhase p=phase(); p.setState("UNKNOWN");assertFalse(AuthOperationTransportPolicy.maySend(p));
  p.setState("INTENT");assertFalse(AuthOperationTransportPolicy.maySend(p));
  p.setState("PREPARED");assertTrue(AuthOperationTransportPolicy.maySend(p));
 }
 @Test public void isolatesParkInstanceWindowAndChannel() {
  SmtAuthTransportPhase a=phase(),b=phase();assertEquals(AuthOperationTransportPolicy.groupKey(a),AuthOperationTransportPolicy.groupKey(b));
  b.setParkId(2);assertNotEquals(AuthOperationTransportPolicy.groupKey(a),AuthOperationTransportPolicy.groupKey(b));
  b=phase();b.setInstanceId("other");assertNotEquals(AuthOperationTransportPolicy.groupKey(a),AuthOperationTransportPolicy.groupKey(b));
  b=phase();b.setOverTime(30L);assertNotEquals(AuthOperationTransportPolicy.groupKey(a),AuthOperationTransportPolicy.groupKey(b));
  b=phase();b.setChannelNo(2);assertNotEquals(AuthOperationTransportPolicy.groupKey(a),AuthOperationTransportPolicy.groupKey(b));
 }
 private SmtAuthTransportPhase phase() {SmtAuthTransportPhase p=new SmtAuthTransportPhase();p.setParkId(1);p.setInstanceId("ISC:1");p.setDeviceId("d");p.setAccessType("ISC");p.setAction("ADD");p.setResourceType("PERSON");p.setServiceType("1");p.setCredentialChannel("FACE");p.setStartTime(1L);p.setOverTime(20L);p.setChannelNo(1);return p;}
}
