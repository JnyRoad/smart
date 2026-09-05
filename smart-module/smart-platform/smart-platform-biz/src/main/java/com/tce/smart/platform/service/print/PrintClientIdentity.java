package com.tce.smart.platform.service.print;
import java.util.*;
/** 设备身份只持有授权集合，不保留或序列化原令牌。 */
public final class PrintClientIdentity {
 public final String deviceIdentity; private final Set<String> parks,printers;
 public PrintClientIdentity(String id,Set<String> parks,Set<String> printers){this.deviceIdentity=id;this.parks=Collections.unmodifiableSet(new HashSet<>(parks));this.printers=Collections.unmodifiableSet(new HashSet<>(printers));}
 public void require(String park,String printer,String device){if(!deviceIdentity.equals(device)||!parks.contains(park)||!printers.contains(printer))throw PrintJobTransactions.error(403,"PRINT_SCOPE_DENIED");}
 public String principal(){return "device:"+PrintJson.hash(deviceIdentity).substring(7,47);}
}
