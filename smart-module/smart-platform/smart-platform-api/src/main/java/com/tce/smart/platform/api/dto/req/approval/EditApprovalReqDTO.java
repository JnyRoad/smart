package com.tce.smart.platform.api.dto.req.approval;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 *
 *
 * @author fushiping
 * @date 2021-04-08 16:25:32
 */
@Data
public class EditApprovalReqDTO extends Model<EditApprovalReqDTO> {
private static final long serialVersionUID = 1L;

	@ApiModelProperty("园区id")
    private Integer parkId;

	@ApiModelProperty("园区名")
    private String parkName;
    /**
   * 事件枚举code  3:物品放行   5:园区报修
   */
	@ApiModelProperty("事件枚举code")
    private Integer eventCode;

	@ApiModelProperty("携带人员")
	private List<String>  badgeList;

}
