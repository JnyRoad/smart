package com.tce.smart.platform.core.entity.print;
import lombok.Data;
/** 打印领域持久记录；物理占用与冻结快照独立保存。 */
@Data
public class PrintPrinter {
    private String printerProfileId;
    private String parkId;
    private String deviceIdentity;
    private String status;
    private String activeJobId;
    private String configJson;
    private Long configRevision;
}
