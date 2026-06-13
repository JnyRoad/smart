package com.tce.smart.platform.api.dto.resp.manage;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.tce.smart.common.core.dto.BaseDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 *
 *
 * @author fushiping
 * @date 2020-07-27 10:45:36
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class EhrSetUpRespDTO extends BaseDTO {
private static final long serialVersionUID = 1L;

    private Long id;
    /**
   * 园区
   */
    private Integer parkId;
    /**
   * 园区名
   */
    private String parkName;
    /**
   * 签收截止时间
   */
    private Integer deadline;

	/**
	 * 自动确认时间
	 */
	private Integer delayLine;

    /**
   * 短信发送是否开启
   */
    private Integer isMessage;
    /**
   * 创建时间
   */
	@TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
   * 设置类型
   */
    private Integer setType;

}
