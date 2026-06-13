package com.tce.smart.platform.core.entity.securityzone;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 *
 *
 * @author fushiping
 * @date 2021-07-29 11:12:53
 */
@Data
@Builder
@TableName("smt_security_auth_relation")
@EqualsAndHashCode(callSuper = true)
public class SmtSecurityAuthRelation extends Model<SmtSecurityAuthRelation> {
private static final long serialVersionUID = 1L;

    /**
   * 主键
   */
	@TableId(value = "id", type = IdType.ID_WORKER)
	private Long id;
    /**
   * 保密区id
   */
    private Long securityId;
    /**
   * 权限id
   */
    private Integer authId;

	/**
	 * 权限名
	 */
    private String authName;

}
