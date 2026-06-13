package com.tce.smart.platform.core.entity.admittance;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 入厂申请预约随行人员表
 *
 * @author fushiping
 * @date 2021-08-17 17:45:13
 */
@Data
@TableName("smt_admittance_fellow")
@EqualsAndHashCode(callSuper = true)
public class SmtAdmittanceFellow extends Model<SmtAdmittanceFellow> {
private static final long serialVersionUID = 1L;

    /**
   * ID
   */
	@TableId(value = "id", type = IdType.ID_WORKER)
    private Long id;
    /**
   * 预约ID
   */
    private Long visitorId;
    /**
   * 姓名
   */
    private String fellowName;
    /**
   * 照片
   */
    private String fellowPhotoId;
    /**
   * 证件号
   */
    private String certNo;
    /**
   * 证件类型
   */
    private Integer certType;

	/**
	 * 访客身份证正面照片
	 */
	private String frontPhotoId;

	/**
	 * 籍贯
	 */
	private String nativePlace;

	/**
	 * 是否为主访客
	 */
	private Integer isMain;
}
