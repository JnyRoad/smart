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
 * @date 2021-04-08 16:25:00
 */
@Data
public class EditApprovalPersonReqDTO extends Model<EditApprovalPersonReqDTO> {
private static final long serialVersionUID = 1L;

	@ApiModelProperty("主键(非必填)")
    private Integer id;
    /**
   * 节点id
   */
	@ApiModelProperty("节点id")
    private Integer nodeId;
    /**
   * 审批人工号
   */
	@ApiModelProperty("审批人工号")
    private String approverBadge;
    /**
   * 审批人姓名
   */
	@ApiModelProperty("审批人姓名")
    private String approverName;
    /**
   * 审批人顺序
   */
	@ApiModelProperty("审批人顺序")
    private Integer sort;
    /**
   * 审批结果
   */
	@ApiModelProperty("审批结果(非必填)")
    private Integer result;


}
