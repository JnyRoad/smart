package com.tce.smart.platform.core.entity.print;

import lombok.Data;
import java.sql.Timestamp;

/** 预览只持久保存归属和制品元数据，PDF 由受控文件服务管理。 */
@Data
public class PrintPreview {
    private String previewId;
    private String parkId;
    private String createdBy;
    private Timestamp createdAt;
    private String status;
    private String detailsJson;
}
