package com.tce.smart.platform.api.dto.resp.approval;

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
 * @date 2021-04-08 16:25:32
 */
@Data
public class ApprovalRespDTO extends Model<ApprovalRespDTO> {
private static final long serialVersionUID = 1L;

    /**
   * ID
   */
	@ApiModelProperty("ID")
    private Integer id;
    /**
   * 园区id
   */
	@ApiModelProperty("园区id")
    private Integer parkId;
    /**
   * 园区名
   */
	@ApiModelProperty("园区名")
    private String parkName;

}
