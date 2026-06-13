package com.tce.smart.platform.core.dto;

import javax.validation.constraints.NotBlank;

import com.tce.smart.platform.core.entity.SmtStaff;

import lombok.Builder;
import lombok.Data;



@Data
public class ApplicationStaffDTO extends SmtStaff {


	/**
	 * 应聘消息的ID
	 */
	@NotBlank(message = "所属应聘id不能为空")
	private Long applicationId;

}
