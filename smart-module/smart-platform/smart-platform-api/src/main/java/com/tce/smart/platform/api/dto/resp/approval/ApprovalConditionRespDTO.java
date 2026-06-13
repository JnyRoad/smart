package com.tce.smart.platform.api.dto.resp.approval;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 *
 *
 * @author fushiping
 * @date 2021-04-08 16:25:24
 */
@Data
@TableName("smt_approval_condition")
@EqualsAndHashCode(callSuper = true)
public class ApprovalConditionRespDTO extends Model<ApprovalConditionRespDTO> {
private static final long serialVersionUID = 1L;

    /**
   *
   */
	@ApiModelProperty("id")
    private Integer id;
    /**
   * 节点id
   */
	@ApiModelProperty("节点id")
    private Integer nodeId;
    /**
   * 条件类型code
   */
	@ApiModelProperty("条件类型code")
    private Integer conditionType;
    /**
   * 条件比较符
   */
	@ApiModelProperty("条件比较符")
    private Integer comparator;
    /**
   * 对比值
   */
	@ApiModelProperty("对比值")
    private String compareValue;
    /**
   * 连接符
   */
	@ApiModelProperty("连接符")
    private Integer connector;
    /**
   * 条件顺序
   */
	@ApiModelProperty("条件顺序")
    private Integer sort;

}
