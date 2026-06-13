package com.tce.smart.platform.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.platform.core.dto.AlarmRecordDTO;
import com.tce.smart.platform.core.entity.SmtAlarmRecord;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 警报记录表
 *
 * @author 王艳勇
 * @date 2019-04-15 11:34:38
 */
public interface SmtAlarmRecordService extends IService<SmtAlarmRecord> {

	boolean saveSmtAlarmRecord(SmtAlarmRecord smtAlarmRecord);

	IPage<List<SmtAlarmRecord>> getAlarmRecord(Page page, @Param("query") AlarmRecordDTO alarmRecordDTO,String[] alarmTime);
}
