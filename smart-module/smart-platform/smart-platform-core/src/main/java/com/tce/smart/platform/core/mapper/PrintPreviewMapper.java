package com.tce.smart.platform.core.mapper;

import com.tce.smart.platform.core.entity.print.PrintPreview;
import org.apache.ibatis.annotations.Param;

/** 预览元数据的持久化边界；不存储 PDF 二进制。 */
public interface PrintPreviewMapper {
    int insertPreview(PrintPreview preview);
    PrintPreview findPreview(@Param("id") String id);
}
