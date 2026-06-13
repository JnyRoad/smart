package com.tce.smart.platform.api.dto.req.approval;

import com.baomidou.mybatisplus.annotation.TableId;
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
public class EditApprovalConditionReqDTO extends Model<EditApprovalConditionReqDTO> {
private static final long serialVersionUID = 1L;

	@ApiModelProperty("主键(非必填)")
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
