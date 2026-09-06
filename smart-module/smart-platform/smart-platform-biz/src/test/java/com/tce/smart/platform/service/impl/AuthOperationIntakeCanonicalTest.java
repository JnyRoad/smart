package com.tce.smart.platform.service.impl;
import com.tce.smart.platform.dto.authoperation.AuthOperationIntakeCommand;
import org.junit.Test;
import org.junit.Assert;
import java.util.Arrays;
/** 请求身份应独立于集合顺序与瞬时业务快照。 */
public class AuthOperationIntakeCanonicalTest {
 private AuthOperationIntakeCommand remove(){return AuthOperationIntakeCommand.builder().requestKey("stable-client-key").requestKind("REMOVE_ROWS").authId(7).authorityType(1).rowIds(Arrays.asList(9,3,9)).build();}
 @Test public void distinctBusinessIntentsCannotShareFingerprint(){
  Assert.assertNotEquals(AuthOperationIntakeCanonical.fingerprint(remove()),AuthOperationIntakeCanonical.fingerprint(remove().toBuilder().authId(8).build()));
 }
 @Test public void orderingDuplicatesAndClientKeyDoNotChangeOriginalIntent(){
  Assert.assertEquals(AuthOperationIntakeCanonical.fingerprint(remove()),AuthOperationIntakeCanonical.fingerprint(remove().toBuilder().requestKey("another-client-key").clearRowIds().rowIds(Arrays.asList(3,9)).build()));
 }
 @Test public void emptyDeletionAndNonPersonTypeAreRejected(){
  for(AuthOperationIntakeCommand invalid:Arrays.asList(remove().toBuilder().clearRowIds().build(),remove().toBuilder().authorityType(2).build())){
   try{AuthOperationIntakeCanonical.fingerprint(invalid);Assert.fail("无效原始请求不能生成可受理指纹");}catch(IllegalArgumentException expected){}
  }
 }
}
