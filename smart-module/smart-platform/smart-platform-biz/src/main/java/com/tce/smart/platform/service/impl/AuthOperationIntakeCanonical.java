package com.tce.smart.platform.service.impl;
import com.tce.smart.platform.dto.authoperation.AuthOperationIntakeCommand;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
/** 原始请求规范摘要；不读取动态业务快照、当前时间或允许园区。 */
public final class AuthOperationIntakeCanonical {
 private AuthOperationIntakeCanonical() { }
 public static String fingerprint(AuthOperationIntakeCommand request) {
  require(request!=null,"请求不能为空");
  require(request.getRequestKey()!=null && request.getRequestKey().matches("[A-Za-z0-9_-]{16,64}"),"请求键格式无效");
  require(request.getAuthId()!=null && request.getAuthId()>0,"权限组ID无效");
  // 现有DeviceAuthTypeEnum中人员权限代码为1；访客不能从人员入口受理。
  require(Integer.valueOf(1).equals(request.getAuthorityType()),"此请求仅支持人员权限");
  boolean remove="REMOVE_ROWS".equals(request.getRequestKind());
  require(remove || "CLEAR_AUTHORITY".equals(request.getRequestKind()),"不支持的受理意图");
  TreeSet<Integer> rows=new TreeSet<>();
  for(Integer id:request.getRowIds()){require(id!=null && id>0,"来源ID无效");rows.add(id);}
  require(remove?!rows.isEmpty():rows.isEmpty(),"删除选择或清空范围无效");
  StringBuilder out=new StringBuilder(tuple(1,"EMPLOYEE_AUTH",request.getRequestKind(),request.getAuthId(),"STAFF",request.getAuthorityType(),remove?"IDS":"ALL_AT_ACCEPTANCE",rows.size()));
  for(Integer id:rows)out.append(tuple(id));
  return hash(out.toString());
 }
 static String tuple(Object... values){StringBuilder b=new StringBuilder();for(Object value:values){String s=value==null?null:String.valueOf(value);b.append(s==null?"-1:":s.length()+":"+s);}return b.toString();}
 static String hash(String value){try{byte[] bytes=MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));StringBuilder out=new StringBuilder();for(byte b:bytes)out.append(String.format(Locale.ROOT,"%02x",b&255));return out.toString();}catch(java.security.NoSuchAlgorithmException e){throw new IllegalStateException(e);}}
 private static void require(boolean condition,String message){if(!condition)throw new IllegalArgumentException(message);}
}
