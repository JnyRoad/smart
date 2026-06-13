package com.tce.smart.platform.api.dto.resp;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.tce.smart.common.core.dto.BaseDTO;
import lombok.Data;

/**
 * 外部部门
 *
 * @author
 * @date 2019-04-15 11:34:58
 */
@Data
public class ExternalDeptRespDTO extends BaseDTO {

	@JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    private String deptName ;

	@JsonSerialize(using = ToStringSerializer.class)
    private Long parentDept;

    private String directorName;

    private String compId;

    private String director;

    private String directorBadge;

    private String c6DeptNo;


}
