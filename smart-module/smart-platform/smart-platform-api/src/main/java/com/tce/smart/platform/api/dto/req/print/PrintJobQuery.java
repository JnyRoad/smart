package com.tce.smart.platform.api.dto.req.print;
import lombok.Data;
/** 任务列表只接受受控筛选条件，人员快照不进入列表查询参数。 */
@Data public class PrintJobQuery { private String parkId;private String printerProfileId;private String status;private String subjectId;private String printItemType;private String createdFrom;private String createdTo;private int page=1;private int size=20; }
