package com.tce.smart.platform.core.service.impl;
import com.tce.smart.platform.core.entity.*;
import com.tce.smart.platform.core.mapper.AuthOperationDirectClaimMapper;
import com.tce.smart.platform.core.dto.authselection.AuthSelection.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.dao.DuplicateKeyException;
import java.util.*;
import java.nio.charset.StandardCharsets;
import java.time.*;
/** 只做不可变物理归属；调用方必须把所有键与发送意图放在同一个事务中。 */
@Service
public class AuthOperationDirectClaimService {
 private final AuthOperationDirectClaimMapper mapper;
 public AuthOperationDirectClaimService(AuthOperationDirectClaimMapper mapper){this.mapper=mapper;}
 @Transactional(propagation=Propagation.MANDATORY,rollbackFor=Exception.class)
 public void claim(List<SmtAuthTransportPhase> phases){
  require(TransactionSynchronizationManager.isActualTransactionActive(),"DIRECT_CLAIM_TRANSACTION_REQUIRED");
  require(phases!=null&&!phases.isEmpty()&&phases.size()<=200,"DIRECT_CLAIM_BATCH_LIMIT");
  SortedMap<String,SmtAuthDirectClaim> all=new TreeMap<>();
  for(SmtAuthTransportPhase supplied:phases){require(supplied!=null&&supplied.getId()!=null,"DIRECT_CLAIM_PHASE_UNVERIFIED");
   SmtAuthTransportPhase p=mapper.phase(supplied.getId());require(p!=null&&sameProjection(p,supplied),"DIRECT_CLAIM_PHASE_UNVERIFIED");require(p!=null&&p.getId()!=null&&p.getDeviceId()!=null&&"DIRECT".equals(p.getAccessType())&&"DIRECT_SEND".equals(p.getPhase())&&Arrays.asList("PREPARED","INTENT","UNKNOWN","ACCEPTED","FINISHED").contains(p.getState()),"DIRECT_CLAIM_PHASE_UNVERIFIED");
   Credential c=AuthTransportCredentials.fromPhase(p);String card=AuthTransportCredentials.card(c);require(card.matches("0|[1-9][0-9]{0,30}"),"DIRECT_CARD_FORMAT_UNVERIFIED");String plate=c instanceof VehicleCredential?((VehicleCredential)c).getPlate():null;
   if(plate!=null)require(plate.equals(plate.trim())&&!java.util.regex.Pattern.compile("[a-z\\s]").matcher(plate).find(),"DIRECT_PLATE_FORMAT_UNVERIFIED");
   String wire=hash(tuple("DIRECT_WIRE_V1",c instanceof VehicleCredential?"VEHICLE":"PERSON",card,plate==null?"":plate));
   for(String kind:plate==null?Collections.singletonList("CARD_NO"):Arrays.asList("CARD_NO","PLATE")){
    SmtAuthDirectClaim n=new SmtAuthDirectClaim();n.setDeviceId(p.getDeviceId());n.setKeyKind(kind);n.setKeyValue("PLATE".equals(kind)?plate:card);n.setId(hash(tuple("DIRECT_CLAIM_V1",n.getDeviceId(),kind,n.getKeyValue())));
    n.setSubjectType(p.getSubjectType());n.setSubjectId(p.getSubjectId());n.setParkId(p.getParkId());n.setInstanceId(p.getInstanceId());n.setResourceId(p.getResourceId());n.setWireHash(wire);n.setFirstPhaseId(p.getId());n.setProofKind("PREPARED".equals(p.getState())?"PHASE_INTENT":"PHASE_HISTORY");n.setCreateTime(LocalDateTime.now(ZoneOffset.UTC));
    SmtAuthDirectClaim previous=all.putIfAbsent(n.getId(),n);if(previous!=null)matches(previous,n);
   }
  }
  for(SmtAuthDirectClaim n:all.values()){
   SmtAuthDirectClaim old=mapper.lock(n.getId());
   if(old==null){try{require(mapper.insert(n)==1,"DIRECT_CLAIM_PHASE_UNVERIFIED");continue;}catch(DuplicateKeyException raced){old=mapper.lock(n.getId());}}
   require(old!=null,"DIRECT_CLAIM_CANONICAL_CONFLICT");matches(old,n);
  }
 }
 private static boolean sameProjection(SmtAuthTransportPhase p,SmtAuthTransportPhase q){return Objects.equals(p.getTargetId(),q.getTargetId())&&Objects.equals(p.getAttemptId(),q.getAttemptId())&&Objects.equals(p.getTaskId(),q.getTaskId())&&Objects.equals(p.getSubjectType(),q.getSubjectType())&&Objects.equals(p.getSubjectId(),q.getSubjectId())&&Objects.equals(p.getParkId(),q.getParkId())&&Objects.equals(p.getInstanceId(),q.getInstanceId())&&Objects.equals(p.getDeviceId(),q.getDeviceId())&&Objects.equals(p.getResourceId(),q.getResourceId())&&Objects.equals(p.getResourceGeneration(),q.getResourceGeneration())&&Objects.equals(p.getCardNo(),q.getCardNo())&&Objects.equals(p.getCredentialVersion(),q.getCredentialVersion())&&Objects.equals(p.getCredentialSnapshot(),q.getCredentialSnapshot());}
 private static void matches(SmtAuthDirectClaim a,SmtAuthDirectClaim b){require(Objects.equals(a.getDeviceId(),b.getDeviceId())&&Objects.equals(a.getKeyKind(),b.getKeyKind())&&Objects.equals(a.getKeyValue(),b.getKeyValue()),"DIRECT_CLAIM_CANONICAL_CONFLICT");require(Objects.equals(a.getSubjectType(),b.getSubjectType())&&Objects.equals(a.getSubjectId(),b.getSubjectId()),"DIRECT_IDENTITY_CONFLICT");require(Objects.equals(a.getResourceId(),b.getResourceId()),"DIRECT_PHYSICAL_RESOURCE_CONFLICT");require(Objects.equals(a.getWireHash(),b.getWireHash()),"DIRECT_CREDENTIAL_BINDING_CHANGED");require(Objects.equals(a.getParkId(),b.getParkId())&&Objects.equals(a.getInstanceId(),b.getInstanceId()),"DIRECT_SCOPE_CHANGED");}
 private static String tuple(String... values){StringBuilder s=new StringBuilder();for(String v:values)s.append(v.getBytes(StandardCharsets.UTF_8).length).append(':').append(v);return s.toString();}
 private static String hash(String text){try{byte[] b=java.security.MessageDigest.getInstance("SHA-256").digest(text.getBytes(StandardCharsets.UTF_8));StringBuilder s=new StringBuilder();for(byte x:b)s.append(String.format("%02x",x&255));return s.toString();}catch(java.security.NoSuchAlgorithmException e){throw new IllegalStateException(e);}}
 private static void require(boolean ok,String reason){if(!ok)throw new IllegalArgumentException(reason);}
}
