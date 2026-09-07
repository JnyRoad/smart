package com.tce.smart.platform.core.service.impl;
import com.tce.smart.platform.core.config.AuthOperationProperties;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import javax.sql.DataSource;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
/** 迁移前不访问新业务表；灰度关闭后，历史接管园区的保护继续有效。 */
@Service
public class AuthOperationTransportGuard {
 private AuthOperationDirectTakeoverService directTakeover;
 @org.springframework.beans.factory.annotation.Autowired public void setDirectTakeover(AuthOperationDirectTakeoverService service){this.directTakeover=service;}
 public com.tce.smart.platform.core.dto.authtransport.AuthDirectTakeover.Decision admitLegacyDirect(Integer task,com.tce.smart.platform.core.dto.authtransport.AuthDirectTakeover.LegacyIdentity identity){return takeover().admitLegacyDirect(task,identity);}
 public com.tce.smart.platform.core.dto.authtransport.AuthDirectTakeover.Decision admitLegacyReceipt(Integer task,String serial,Integer code,String digest){return takeover().admitLegacyReceipt(task,serial,code,digest);}
 private AuthOperationDirectTakeoverService takeover(){if(directTakeover==null)throw new IllegalStateException("DIRECT 持久门禁未装配");return directTakeover;}
 private final JdbcTemplate jdbc;private final AuthOperationProperties properties;private volatile Boolean installed;
 public AuthOperationTransportGuard(DataSource source,AuthOperationProperties properties){this.jdbc=new JdbcTemplate(source);this.properties=properties;}
 public boolean installed(){Boolean b=installed;if(b==null){synchronized(this){if(installed==null)installed=jdbc.queryForObject("SELECT COUNT(*) FROM USER_TABLES WHERE TABLE_NAME='SMT_AUTH_TRANSPORT_PHASE'",Integer.class)>0;b=installed;}}return b;}
 public boolean bound(String access,String task){return task!=null&&installed()&&jdbc.queryForObject("SELECT COUNT(*) FROM SMT_AUTH_TRANSPORT_PHASE WHERE ACCESS_TYPE=? AND TASK_ID=?",Integer.class,access,task)>0;}
 /** 无冻结来源的旧任务不能在已持记录锁后再倒拿主体锁；受保护园区统一保留核验。 */
 public boolean protectSource(String device,String card,String general){
  if(!installed())return false;
  List<Integer> parks=jdbc.queryForList("SELECT PARK_ID FROM SMT_DEVICE WHERE ID=?",Integer.class,device);
  if(parks.size()!=1){
   parks=jdbc.queryForList("SELECT DISTINCT PARK_ID FROM (SELECT PARK_ID FROM SMT_AUTH_TRANSPORT_PHASE WHERE DEVICE_ID=? UNION SELECT PARK_ID FROM SMT_AUTH_RESOURCE_COORD WHERE DEVICE_ID=? UNION SELECT PARK_ID FROM SMT_TASK_DOWN_RECORD WHERE DEVICE_CODE=? UNION SELECT PARK_ID FROM SMT_ISC_DOWN_RECORD WHERE DEVICE_CODE=?) WHERE PARK_ID IS NOT NULL AND ROWNUM<=2",Integer.class,device,device,device,device);
   if(parks.size()!=1){review(null,"SOURCE",device,card,"LEGACY_PARK_OWNERSHIP_UNKNOWN");return true;}
  }
	Integer park=parks.get(0);
	if(park==null||park<=0){review(null,"SOURCE",device,card,"LEGACY_PARK_OWNERSHIP_UNKNOWN");return true;}
  boolean protectedPark=properties.enabledForPark(park)||jdbc.queryForObject("SELECT (SELECT COUNT(*) FROM SMT_AUTH_SELECTION_SOURCE WHERE PARK_ID=?)+(SELECT COUNT(*) FROM SMT_AUTH_SOURCE_COORD WHERE PARK_ID=?)+(SELECT COUNT(*) FROM SMT_AUTH_TRANSPORT_PHASE WHERE PARK_ID=?) FROM DUAL",Integer.class,park,park,park)>0;
  if(!protectedPark)return false;
  review(park,"SOURCE",device,card,"LEGACY_SOURCE_SNAPSHOT_MISSING");return true;
 }
 /** 稳定键合并重复证据，不因同一回执反复到达堆积核验行。 */
 public void review(Integer park,String access,String device,String task,String reason){
  takeover().review(park,access,device,task,reason);
 }
 /** 仅供已授权的全局治理调用；无可信园区时不得挂入任一园区用户的结果。 */
 public List<Map<String,Object>> unknownReviews(String after,int limit){if(limit<=0||limit>200)throw new IllegalArgumentException("核验分页越界");if(!installed())return Collections.emptyList();return jdbc.queryForList("SELECT * FROM (SELECT ID,PARK_ID,ACCESS_TYPE,DEVICE_ID,TASK_KEY,REASON,STATE,CREATE_TIME FROM SMT_AUTH_TRANSPORT_REVIEW WHERE PARK_ID IS NULL AND (? IS NULL OR ID>?) ORDER BY ID) WHERE ROWNUM<=?",after,after,limit);}
 public List<Map<String,Object>> reviews(int park,String after,int limit){if(park<=0||limit<=0||limit>200)throw new IllegalArgumentException("核验分页越界");if(!installed())return Collections.emptyList();return jdbc.queryForList("SELECT * FROM (SELECT ID,PARK_ID,ACCESS_TYPE,DEVICE_ID,TASK_KEY,REASON,STATE,CREATE_TIME FROM SMT_AUTH_TRANSPORT_REVIEW WHERE PARK_ID=? AND (? IS NULL OR ID>?) ORDER BY ID) WHERE ROWNUM<=?",park,after,after,limit);}
 private static String part(String s){return s==null?"-1:":s.length()+":"+s;}
 private static String hash(String text){try{byte[] bytes=MessageDigest.getInstance("SHA-256").digest(text.getBytes(StandardCharsets.UTF_8));StringBuilder b=new StringBuilder();for(byte x:bytes)b.append(String.format("%02x",x&255));return b.toString();}catch(Exception e){throw new IllegalStateException(e);}}
}
