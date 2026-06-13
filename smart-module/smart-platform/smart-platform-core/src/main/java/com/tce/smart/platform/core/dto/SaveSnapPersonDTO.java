package com.tce.smart.platform.core.dto;
import com.baomidou.mybatisplus.extension.activerecord.Model;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 人员抓拍记录表
 *
 * @author 梁圆
 * @date 2019-04-13 18:18:30
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SaveSnapPersonDTO extends Model<SaveSnapPersonDTO> {
private static final long serialVersionUID = 1L;

    /**
   * 设备ID
   */
    private String deviceId;

    /**
   *
   */
    private Integer eventType;
    /**
   * 通过时间
   */
    private String snapTime;
    /**
   *
   */
    private String snapPhotoId;

    /**
   * 卡片id
   */
    private String cardNo;
    /**
     * 是否放行
     */
    private Integer letPass;

	/**
	 * 体温
	 */
	private Double faceTemperature;
}
