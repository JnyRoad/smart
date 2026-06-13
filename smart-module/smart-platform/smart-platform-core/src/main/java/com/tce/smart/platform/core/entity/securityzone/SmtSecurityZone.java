package com.tce.smart.platform.core.entity.securityzone;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 *
 *
 * @author fushiping
 * @date 2021-07-29 11:12:46
 */
@Data
@TableName("smt_security_zone")
@EqualsAndHashCode(callSuper = true)
public class SmtSecurityZone extends Model<SmtSecurityZone> {
private static final long serialVersionUID = 1L;

    /**
   * id
   */
	@TableId(value = "id", type = IdType.ID_WORKER)
	private Long id;
    /**
   * 保密区名
   */
    private String securityName;
    /**
   * 保密区code
   */
    private String securityCode;
    /**
   * 空白字段
   */
    private String blank1;
    /**
   * 园区id
   */
    private Integer parkId;
    /**
   * 创建时间
   */
    private LocalDateTime createTime;
    /**
   * 修改时间
   */
    private LocalDateTime updateTime;

}
