package com.tce.smart.platform.core.service.impl;

import com.tce.smart.platform.core.dto.authtransport.AuthDirectTakeover.*;
import com.tce.smart.platform.core.entity.SmtAuthTransportPhase;
import com.tce.smart.platform.core.entity.SmtDeviceTask;
import com.tce.smart.platform.core.mapper.AuthOperationDirectTakeoverMapper;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

/** 每次读取持久能力与设备历史；独立短事务结束后才返回发送许可。 */
@Service
public class AuthOperationDirectTakeoverService {
    private final AuthOperationDirectTakeoverMapper mapper;
    private final TransactionTemplate isolated;
    public AuthOperationDirectTakeoverService(AuthOperationDirectTakeoverMapper mapper,PlatformTransactionManager manager) {
        this.mapper=mapper;this.isolated=new TransactionTemplate(manager);
        isolated.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);isolated.setTimeout(5);
    }
    /** 启动及新 DIRECT INTENT 的能力门禁；不修改 route，也不替代调用方的新工作开关。 */
    public void assertDirectSendEnabled(int park,String instance) {
        if(park<=0 || !present(instance))throw new IllegalArgumentException("DIRECT 路由参数不完整");
        isolated.execute(status->{RouteCapability r=mapper.route(park);
            if(r==null || !Integer.valueOf(1).equals(r.getDirectTakeoverVersion()) || !instance.equals(r.getInstanceId()))
                throw new IllegalStateException("DIRECT 持久接管能力或实例归属未就绪");
            return null;});
    }
    public Decision admitLegacyDirect(Integer taskId,LegacyIdentity identity) {
        return admit(taskId,identity,null,false);
    }
    public Decision admitLegacyReceipt(Integer taskId,String serial,Integer code,String digest) {
        if(code==null || !present(serial))return reject(null,null,String.valueOf(taskId),serial,"LEGACY_RECEIPT_INVALID");
        return admit(taskId,null,serial,true);
    }
    private Decision admit(Integer id,LegacyIdentity identity,String serial,boolean receipt) {
        try {
            return isolated.execute(status->{
                SmtDeviceTask t=id==null?null:mapper.task(id);
                if(t==null)return reviewDecision(null,null,String.valueOf(id),serial,"LEGACY_TASK_UNKNOWN");
                String device=t.getDeviceCode(),task=String.valueOf(id),frozenSerial=t.getSerialNo();
                if(!present(device)||!present(frozenSerial)||!present(t.getCardNo())||t.getServiceType()==null||action(t.getAction())==null)
                    return reviewDecision(null,device,task,frozenSerial,"LEGACY_IDENTITY_INCOMPLETE");
                if(receipt ? !Objects.equals(serial,frozenSerial)||!Integer.valueOf(1).equals(t.getDeviceType())
                        : identity==null || !LegacyIdentity.of(t).equals(identity.toBuilder().wirePark(null).wireEnvelopeDevice(null).wireDevice(null).wireCard(null)
                            .wireSerial(null).wireTask(null).wireOperation(null).wireGeneral(null).wireCardType(null).wireStart(null).wireEnd(null).build()))
                    return reviewDecision(null,device,task,frozenSerial,"LEGACY_COMMAND_MISMATCH");
                List<SmtAuthTransportPhase> phases=mapper.taskPhases(task);
                if(phases==null)throw new IllegalStateException("阶段查询无结果容器");
                if(!phases.isEmpty()) {
                    if(phases.size()!=1 || !matchesPhase(phases.get(0),t))return reviewDecision(null,device,task,frozenSerial,"LEGACY_PHASE_MISMATCH");
                    return Decision.builder().outcome(Outcome.OWNED_BY_TRANSPORT).phase(phases.get(0)).reason("DIRECT_PHASE_OWNED").build();
                }
                if(mapper.deviceHistory(device)>0)return reviewDecision(null,device,task,frozenSerial,"DIRECT_DEVICE_HISTORY_PROTECTED");
                List<Integer> parks=mapper.deviceParks(device);
                if(parks==null || parks.size()!=1 || parks.get(0)==null || parks.get(0)<=0)
                    return reviewDecision(null,device,task,frozenSerial,"LEGACY_DEVICE_PARK_UNKNOWN");
                Integer park=parks.get(0);RouteCapability r=mapper.route(park);
                if(r==null || !present(r.getInstanceId()) || r.getDirectTakeoverVersion()==null)
                    return reviewDecision(park,device,task,frozenSerial,"DIRECT_CAPABILITY_UNKNOWN");
                if(r.getDirectTakeoverVersion()!=0)return reviewDecision(park,device,task,frozenSerial,
                    r.getDirectTakeoverVersion()==1?"DIRECT_PARK_PROTECTED":"DIRECT_CAPABILITY_UNKNOWN");
                if(!receipt && !wireMatches(t,identity,park))return reviewDecision(park,device,task,frozenSerial,"LEGACY_WIRE_MISMATCH");
                return Decision.builder().outcome(Outcome.LEGACY_ALLOWED).reason("DIRECT_KNOWN_LEGACY").build();
            });
        } catch(RuntimeException unavailable) {
            // 路由行的 WAIT 超时后不能再次写入可能被同一事务占用的留痕表；直接失败关闭。
            return Decision.builder().outcome(Outcome.VERIFYING).reason("DIRECT_GATE_UNAVAILABLE").build();
        }
    }
    private Decision reject(Integer park,String device,String task,String serial,String reason) {
        try {return isolated.execute(status->reviewDecision(park,device,task,serial,reason));}
        catch(RuntimeException failure){return Decision.builder().outcome(Outcome.VERIFYING).reason("DIRECT_GATE_UNAVAILABLE").build();}
    }
    private Decision reviewDecision(Integer park,String device,String task,String serial,String reason) {
        saveReview(park,"DIRECT",device,task,serial,reason);
        return Decision.builder().outcome(Outcome.VERIFYING).reason(reason).build();
    }
    /** 调用方随后回滚，不能撤销已提交的核验；稳定键不含可变园区。 */
    public void review(Integer park,String access,String device,String task,String reason) {
        isolated.execute(status->{saveReview(park,access,device,task,null,reason);return null;});
    }
    private void saveReview(Integer park,String access,String device,String task,String serial,String reason) {
        String key=hash(part(access)+part(device)+part(task)+part(serial)+part(reason));
        try {mapper.review(key,park,access,device,task,reason);}
        catch(DuplicateKeyException raced){if(mapper.reviewExists(key)!=1)throw raced;}
    }
    /** 只核已有精确上下文，不能用任意非空 ThreadLocal 放行业务成功。 */
    public static boolean matchesPhase(SmtAuthTransportPhase p,SmtDeviceTask t) {
        return p!=null && t!=null && p.getId()!=null && p.getTargetId()!=null && p.getAttemptId()!=null
            && p.getAttemptNo()!=null && present(p.getLeaseToken()) && p.getParkId()!=null && present(p.getInstanceId())
            && "DIRECT".equals(p.getAccessType()) && "DIRECT_SEND".equals(p.getPhase())
            && Objects.equals(p.getTaskId(),String.valueOf(t.getId())) && Objects.equals(p.getSerialNo(),t.getSerialNo())
            && Objects.equals(p.getDeviceId(),t.getDeviceCode()) && Objects.equals(p.getCardNo(),t.getCardNo())
            && Objects.equals(p.getAction(),action(t.getAction())) && Objects.equals(p.getServiceType(),String.valueOf(t.getServiceType()))
            && Objects.equals(p.getStartTime(),t.getStartTime()) && Objects.equals(p.getOverTime(),t.getOverTime())
            && (("PERSON".equals(p.getResourceType()) && "FACE".equals(p.getCredentialChannel()) && Integer.valueOf(1).equals(t.getDeviceType()))
                || ("VEHICLE".equals(p.getResourceType()) && "PLATE".equals(p.getCredentialChannel()) && Integer.valueOf(2).equals(t.getDeviceType())));
    }
    private static boolean wireMatches(SmtDeviceTask t,LegacyIdentity i,Integer park) {
        if(i.getWireOperation()==null)return true;
        if(!Objects.equals(i.getWireEnvelopeDevice(),i.getWireDevice())||!Objects.equals(park,i.getWirePark())||!Objects.equals(t.getDeviceCode(),i.getWireDevice())||!Objects.equals(t.getCardNo(),i.getWireCard()))return false;
        String op=i.getWireOperation(),a=action(t.getAction());
        if(Integer.valueOf(2).equals(t.getDeviceType()))return "CAR_DELETE".equals(op)&&"DELETE".equals(a)
            || "CAR_ADD".equals(op)&&"ADD".equals(a)&&Objects.equals(t.getGeneral(),i.getWireGeneral())&&Objects.equals(t.getCardType(),i.getWireCardType());
        if(!Integer.valueOf(1).equals(t.getDeviceType()) || !Objects.equals(t.getSerialNo(),i.getWireSerial())||!Objects.equals(t.getId(),i.getWireTask()))return false;
        if("CARD_DELETE".equals(op))return "DELETE".equals(a);
        if("CARD_UPDATE".equals(op))return "UPDATE".equals(a);
        return "CARD_ADD".equals(op)&&"ADD".equals(a)&&Objects.equals(t.getGeneral(),i.getWireGeneral())
            &&Objects.equals(t.getCardType(),i.getWireCardType())&&Objects.equals(t.getStartTime(),i.getWireStart())&&Objects.equals(t.getOverTime(),i.getWireEnd());
    }
    private static String action(Integer a) {return a==null?null:a==1||a==11?"ADD":a==2||a==12?"DELETE":a==3||a==13?"UPDATE":null;}
    private static boolean present(String s){return s!=null&&!s.trim().isEmpty();}
    private static String part(String s){return s==null?"-1:":s.length()+":"+s;}
    private static String hash(String s){try{byte[] bytes=MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8));StringBuilder out=new StringBuilder();for(byte b:bytes)out.append(String.format("%02x",b&255));return out.toString();}catch(Exception e){throw new IllegalStateException(e);}}
}
