package com.tce.smart.platform.core.service.impl;
import com.tce.smart.platform.core.entity.SmtAuthTransportPhase;
/** 仅在工作流已通过版本门禁的本库事务内有效，退出时必定清除。 */
public final class AuthOperationTransportRecordContext implements AutoCloseable {
 private static final ThreadLocal<SmtAuthTransportPhase> CURRENT=new ThreadLocal<>();
 private AuthOperationTransportRecordContext(SmtAuthTransportPhase p) {if(CURRENT.get()!=null)throw new IllegalStateException("禁止嵌套回执上下文");CURRENT.set(p);}
 public static AuthOperationTransportRecordContext open(SmtAuthTransportPhase p){return new AuthOperationTransportRecordContext(p);}
 public static SmtAuthTransportPhase current(String access,String task) {SmtAuthTransportPhase p=CURRENT.get();return p!=null&&access.equals(p.getAccessType())&&task.equals(p.getTaskId())?p:null;}
 @Override public void close(){CURRENT.remove();}
}
