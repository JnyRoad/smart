package com.tce.smart.platform.core.entity.securityzone;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

/**
 *
 *OA同步区域与权限关联表
 * @author fushiping
 * @date 2021-07-29 11:13:44
 */
@Data
@Builder
@TableName("smt_oa_area_relation")
@EqualsAndHashCode(callSuper = true)
public class SmtOaAreaRelation extends Model<SmtOaAreaRelation> {

private static final long serialVersionUID = 1L;

	@TableId(value = "id", type = IdType.ID_WORKER)
	private Long id;
    /**
   * oa区域
   */
    private String oaAreaName;
    /**
   * oa区域id
   */
    private Integer oaAreaId;
    /**
   * 园区id
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
