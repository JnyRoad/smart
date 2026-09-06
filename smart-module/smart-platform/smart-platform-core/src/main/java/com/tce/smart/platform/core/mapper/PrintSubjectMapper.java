package com.tce.smart.platform.core.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.*;

/** 人员源只读 SQL；主体命名空间与园区条件在查询入口固定。 */
@Mapper
public interface PrintSubjectMapper {
    List<Map<String,Object>> subject(@Param("park") String park,@Param("type") String type,@Param("id") String id);
    List<Map<String,Object>> staffOrganizations(@Param("park") String park,@Param("company") String company);
    List<Map<String,Object>> cards(@Param("park") String park,@Param("id") String id);
    List<Map<String,Object>> areaAuthorities(@Param("park") String park,@Param("area") String area);
    List<byte[]> photos(@Param("code") String code,@Param("storagePark") String storagePark,@Param("types") List<Integer> types);
    List<Map<String,Object>> search(@Param("park") String park,@Param("type") String type,@Param("keyword") String keyword,@Param("offset") long offset,@Param("end") long end);
    long count(@Param("park") String park,@Param("type") String type,@Param("keyword") String keyword);
}
