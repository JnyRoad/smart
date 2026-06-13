package com.tce.smart.platform.api.dto.req.manage;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.tce.smart.common.core.dto.BaseDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.List;

/**
 *
 *
 * @author fushiping
 * @date 2020-07-27 10:45:36
 */
@Data

public class EditEhrSetUpReqDTO extends BaseDTO {
private static final long serialVersionUID = 1L;

    /**
   * id
   */
    private Long id;
    /**
   * 园区
   */
    private Integer parkId;
    /**
   * 签收截止类型
   */
    private Integer deadlineType;
	/**
	 * 自动确认时间
	 */
    private Integer delayLine;
    /**
   * 签收截止时间
   */
    private Integer deadline;
    /**
   * 短信发送是否开启
   */
    private Integer isMessage;
    /**
   * 设置类型
   */
    private Integer setType;

}
