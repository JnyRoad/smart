package com.tce.smart.platform.api.dto.resp;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.tce.smart.common.core.dto.BaseDTO;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 外部组织关系关联
 *
 * @author
 * @date 2019-04-15 11:34:58
 */
@Data
public class OrganizeRelationRespDTO extends BaseDTO {

	@JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    private Integer userId ;

    private String compName;

    private Integer parkId;

    private String parkName;

    private String userName;

    private String password;

    private String userRole;

    private String source;

    private Integer compType;

    private LocalDateTime createTime;

    private List<Integer> deviceAuthId;

}
