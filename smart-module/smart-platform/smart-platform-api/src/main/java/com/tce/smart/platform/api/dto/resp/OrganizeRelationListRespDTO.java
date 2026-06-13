package com.tce.smart.platform.api.dto.resp;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.tce.smart.common.core.dto.BaseDTO;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 外部组织关系关联
 *
 * @author
 * @date 2019-04-15 11:34:58
 */
@Data
public class OrganizeRelationListRespDTO extends BaseDTO {

	@JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    private String compName;

    private String parkName;

    private String userName;

    private String userRole;

    private String source;

	/**
	 * 企业类型
	 */
	private Integer compType;

	/**
	 * 企业类型描述
	 */
    private String compTypeDesc;


}
