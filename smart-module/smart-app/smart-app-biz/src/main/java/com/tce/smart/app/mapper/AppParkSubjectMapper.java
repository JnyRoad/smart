package com.tce.smart.app.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tce.smart.app.entity.AppParkSubject;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 园区主题
 *
 * @author mingkai.wu
 * @date 2019-04-25 09:44:25
 */
public interface AppParkSubjectMapper extends BaseMapper<AppParkSubject> {
	 void deleteParkById(@Param("id") Integer subjectId);
}
