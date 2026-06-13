package com.tce.smart.platform.core.dto;

import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 设备任务信息表
 *
 * @author 王艳勇
 * @date 2019-04-15 15:09:27
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DeviceTaskQueryDTO extends Model<DeviceTaskQueryDTO> {
private static final long serialVersionUID = 1L;


    /**
     * 设备编码
     */
	@NotNull(message="设备类型不能为空")
    private Integer deviceType;

    /**
     * 卡片Id
     */
	@NotBlank(message="卡片号不能为空")
	@NotNull(message="卡片号不能为空")
    private String cardNo;


}
