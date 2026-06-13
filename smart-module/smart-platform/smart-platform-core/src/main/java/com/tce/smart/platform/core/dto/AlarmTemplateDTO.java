package com.tce.smart.platform.core.dto;

import java.util.List;

import com.tce.smart.platform.core.entity.SmtAlarmRecever;
import com.tce.smart.platform.core.entity.SmtAlarmTemplate;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 发送人员信息
 * @author Lenovo
 *
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AlarmTemplateDTO extends SmtAlarmTemplate {

	private static final long serialVersionUID = 1L;

	/**
	 * 人员集合
	 */
	private List<SmtAlarmRecever> alarmReceverList;
}
