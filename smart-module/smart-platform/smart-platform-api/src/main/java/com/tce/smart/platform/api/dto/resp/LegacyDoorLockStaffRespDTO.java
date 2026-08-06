package com.tce.smart.platform.api.dto.resp;

import lombok.Data;

import java.io.Serializable;

/**
 * 遗留门锁按工号查询的最小员工投影。
 *
 * <p>仅保留现有门锁同步 DTO 已使用的工号、姓名和手机号；身份证号、人脸、住址等
 * SmtStaff 字段禁止因历史兼容而透出。</p>
 */
@Data
public class LegacyDoorLockStaffRespDTO implements Serializable {
	private static final long serialVersionUID = 1L;

	private String badge;
	private String name;
	private String phone;
}
