package com.tce.smart.platform.core.entity.admittance;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 *
 *
 * @author fushiping
 * @date 2021-08-17 17:45:23
 */
@Data
@Builder
@TableName("smt_admittance_area_type_auth")
@EqualsAndHashCode(callSuper = true)
public class SmtAdmittanceAreaTypeAuth extends Model<SmtAdmittanceAreaTypeAuth> {
private static final long serialVersionUID = 1L;

    /**
   * id
   */
	@TableId(value = "id", type = IdType.ID_WORKER)
    private Long id;
	/**
	 * oa区域类型id
	 */
	private Integer areaTypeId;
	/**
	 * 园区ID
	 */
	private Integer parkId;
	/**
	 * 权限策略名
	 */
    private String authName;
    /**
   * 权限策略id
   */
    private Integer authId;
	/**
	 * 权限类型
	 */
    private Integer authType;
    /**
   * 创建时间
   */
	@TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

}
