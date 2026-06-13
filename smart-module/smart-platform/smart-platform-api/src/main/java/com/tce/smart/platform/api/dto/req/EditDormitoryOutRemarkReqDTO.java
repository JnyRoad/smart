package com.tce.smart.platform.api.dto.req;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.tce.smart.common.core.dto.BaseDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * 住宿备注表
 *
 * @author fushiping
 * @date 2020-12-29
 */
@Data
@TableName("SMT_DORMITORY_OUT_REMARK")
public class EditDormitoryOutRemarkReqDTO extends BaseDTO {
	private static final long serialVersionUID = 4692701122505311026L;

    /**
   * 主键
   */
	@ApiModelProperty("主键")
	@JsonFormat(shape=JsonFormat.Shape.STRING)
    private Long id;

    /**
   * 员工住宿表ID
   */
	@ApiModelProperty("员工住宿表ID")
    private Integer dorStaffId;

    /**
   * 离宿类型  1：出差   2：请假   3：调休
   */
	@ApiModelProperty("离宿类型 1：出差   2：请假   3：调休")
    private Integer reasonType;

    /**
   * 开始日期
   */
	@ApiModelProperty("开始日期")
    private Date startTime;

    /**
   * 结束日期
   */
	@ApiModelProperty("结束日期")
    private Date endTime;

    /**
   *  备注
   */
	@ApiModelProperty("备注")
    private String remark;

	/**
	 * 创建时间
	 */
	@ApiModelProperty("创建时间")
	private Date createTime;

	/**
	 * 更新时间
	 */
	@ApiModelProperty("更新时间")
	private Date updateTime;

}
