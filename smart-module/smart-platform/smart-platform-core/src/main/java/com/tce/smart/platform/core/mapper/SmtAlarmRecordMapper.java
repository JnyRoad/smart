package com.tce.smart.platform.core.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.platform.core.dto.AlarmRecordDTO;
import com.tce.smart.platform.core.entity.SmtAlarmRecord;

/**
 * 警报记录表
 *
 * @author 王艳勇
 * @date 2019-04-15 11:34:38
 */
public interface SmtAlarmRecordMapper extends BaseMapper<SmtAlarmRecord> {

	IPage getAlarmRecord(Page page, @Param("query") AlarmRecordDTO alarmRecordDTO);

	List<Integer> getAreaId(@Param("parkId") Integer parkId,@Param("pid") Integer pid);
}
