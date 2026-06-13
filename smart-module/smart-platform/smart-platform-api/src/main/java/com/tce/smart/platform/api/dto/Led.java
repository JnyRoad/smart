package com.tce.smart.platform.api.dto;

import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * LED信息
 */
@Data
public class Led extends Model<Led> {
	private Integer parkId;

    /**
     * 设备编号【必选】
     */
	@NotBlank(message = "设备编号不可为空")
    private String deviceCode;

    /**
     * 显示场景 0：正常场景；1：有权限过车场景；2：无权限过车场景
     */
	@NotNull(message = "显示场景不可为空")
	@Range(min = 0, max = 2, message = "显示场景参数非法")
    private Integer displayScene;

    /**
     * 语音内容
     */
    private String soundText;


    /**
     * 区域信息
     */
	@NotNull(message = "区域信息不可为空")
    private List<LedArea> ledAreaList;

}
