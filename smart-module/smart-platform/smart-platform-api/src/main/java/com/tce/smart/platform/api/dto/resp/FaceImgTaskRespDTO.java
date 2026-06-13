package com.tce.smart.platform.api.dto.resp;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 *
 *
 * @author fushiping
 * @date 2021-07-20 17:44:40
 */
@Data
public class FaceImgTaskRespDTO implements Serializable {
private static final long serialVersionUID = 1L;

    /**
   * id
   */
	@ApiModelProperty("id")
    private Long id;
    /**
	 * 园区id
	 */
	@ApiModelProperty("园区id")
	private Integer parkId;
	/**
	 * 园区
	 */
	@ApiModelProperty("园区名")
	private String parkName;
    /**
   * 任务名称
   */
	@ApiModelProperty("任务名称")
    private String taskName;
    /**
   * 总任务量
   */
	@ApiModelProperty("总任务量")
    private Integer totalNum;
    /**
   * 成功数量
   */
	@ApiModelProperty("成功数量")
    private Integer successNum;

	/**
	 * 成功数量
	 */
	@ApiModelProperty("失败数量")
	private Integer failNum;
    /**
   * 创建时间
   */
	@ApiModelProperty("创建时间")
    private LocalDateTime createTime;

}
