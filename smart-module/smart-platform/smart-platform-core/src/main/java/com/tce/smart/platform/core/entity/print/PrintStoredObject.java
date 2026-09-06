package com.tce.smart.platform.core.entity.print;

import lombok.Data;
import java.sql.Timestamp;

/** 打印域私有不可变对象；只通过授权接口读取，不提供公开文件URL。 */
@Data
public class PrintStoredObject {
    private String objectId;
    private String parkId;
    private String createdBy;
    private String purpose;
    private String accessScope;
    private String ownerId;
    private String contentHash;
    private String mediaType;
    private Long sizeBytes;
    private Timestamp createdAt;
    private byte[] contentBytes;
}
