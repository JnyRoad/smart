package com.tce.smart.platform.core.dto;

import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 设备任务信息表
 *
 * @author 王艳勇
 * @date 2019-04-15 15:09:27
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DeviceTaskDeleteDTO extends Model<DeviceTaskDeleteDTO> {
private static final long serialVersionUID = 1L;


    /**
     * 设备编码
     */
	@NotNull(message="设备编码不能为空")
    private List<String> deviceCode;

    /**
     * 卡片Id
     */
	@NotBlank(message="卡片号不能为空")
	@NotNull(message="卡片号不能为空")
    private String cardNo;

	/**
	 * 任务序列号
	 */
	private String serialNo;

}
