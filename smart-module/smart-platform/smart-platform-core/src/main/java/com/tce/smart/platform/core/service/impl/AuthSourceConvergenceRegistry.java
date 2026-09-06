package com.tce.smart.platform.core.service.impl;
import com.tce.smart.platform.core.dto.authselection.AuthSelection.*;
import com.tce.smart.platform.core.entity.SmtAuthSelectionSource;
import org.springframework.stereotype.Component;
import java.util.*;
/** 来源种类、主体类型、快照版本必须同时匹配；未知或重复注册均关闭成功路径。 */
@Component
public class AuthSourceConvergenceRegistry {
 private final Map<String,AuthSourceHandler<?>> handlers=new HashMap<>();
 public AuthSourceConvergenceRegistry(List<AuthSourceHandler<?>> registered) {
  for(AuthSourceHandler<?> handler:registered) {
   if(handler.sourceKind()==null || handler.subjectType()==null || handler.snapshotVersion()<0 || handler.snapshotType()==null)throw new IllegalArgumentException("来源 handler 注册不完整");
   if(handlers.put(key(handler.sourceKind().name(),handler.snapshotVersion()),handler)!=null)throw new IllegalArgumentException("来源版本重复注册");
  }
 }
 public AuthSourceHandler<?> handler(SmtAuthSelectionSource row) {
  AuthSourceHandler<?> h=handlers.get(key(AuthSelectionSnapshots.kind(row),AuthSelectionSnapshots.version(row)));
  return h!=null && h.subjectType().name().equals(AuthSelectionSnapshots.subject(row))?h:null;
 }
 public boolean apply(SmtAuthSelectionSource row) {
  AuthSourceHandler<?> h=handler(row);if(h==null)return false;return applyTyped(h,row);
 }
 private static <B extends BusinessSnapshot> boolean applyTyped(AuthSourceHandler<B> h,SmtAuthSelectionSource row) {
  return h.applyExact(row,h.snapshotVersion()==0?null:AuthSelectionSnapshots.business(row,h.snapshotType()));
 }
 public <B extends BusinessSnapshot> void lockAndValidate(SourceSelection<B> source) {
  AuthSourceHandler<?> h=handlers.get(key(source.getSourceKind().name(),source.getSnapshotVersion()));
  if(h==null || h.subjectType()!=source.getSubjectType() || source.getBusiness()==null || source.getBusiness().getClass()!=h.snapshotType())throw new IllegalArgumentException("未注册来源类型、版本或业务 DTO");
  lockTyped(h,source);
 }
 @SuppressWarnings("unchecked") private static <B extends BusinessSnapshot> void lockTyped(AuthSourceHandler<?> handler,SourceSelection<B> source){((AuthSourceHandler<B>)handler).lockAndValidate(source);}
 private static String key(String kind,int version){return kind+":"+version;}
}
