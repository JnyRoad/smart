package com.tce.smart.platform.core.mapper;
import com.tce.smart.platform.core.entity.print.PrintStoredObject;
import org.apache.ibatis.annotations.Param;
/** 元数据与BLOB分开读取，权限检查不提前加载文件字节。 */
public interface PrintObjectMapper {
    int insertObject(PrintStoredObject object);
    PrintStoredObject findMetadata(@Param("id") String id);
    PrintStoredObject readContent(@Param("id") String id);
}
