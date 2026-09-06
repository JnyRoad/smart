package com.tce.smart.platform.core.service.impl;
import com.tce.smart.platform.core.entity.SmtAuthTransportPhase;
import java.util.*;
/** 对批量兼容性与未知提交执行纯规则校验。 */
public final class AuthOperationTransportPolicy {
 private AuthOperationTransportPolicy() { }
 public static boolean maySend(SmtAuthTransportPhase p) { return p!=null && "PREPARED".equals(p.getState()); }
 public static String groupKey(SmtAuthTransportPhase p) { StringBuilder b=new StringBuilder(); for(Object v:Arrays.asList(p.getParkId(),p.getInstanceId(),p.getAccessType(),p.getDeviceId(),p.getAction(),p.getResourceType(),p.getServiceType(),p.getCredentialChannel(),p.getChannelNo(),p.getStartTime(),p.getOverTime())) { String x=String.valueOf(v);b.append(x.length()).append(':').append(x); } return b.toString(); }
}
