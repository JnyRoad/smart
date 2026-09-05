package com.tce.smart.platform.core.entity.securityzone;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.*;

import java.time.LocalDateTime;

/**
 *
 *
 * @author fushiping
 * @date 2021-07-29 11:13:24
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("smt_security_auth_delete")
@EqualsAndHashCode(callSuper = true)
public class SmtSecurityAuthDelete extends Model<SmtSecurityAuthDelete> {
private static final long serialVersionUID = 1L;

    /**
   * 主键
   */
	@TableId(value = "id", type = IdType.ID_WORKER)
	private Long id;
    /**
   * 超过多少天后删除权限
   */
    private Integer deleteDay;
    /**
   * 是否计算假期
   */
    private Integer isHoliday;
    /**
   * 是否计算出差
   */
    private Integer isBusiness;
    /**
   * 是否计算请假
   */
    private Integer isLeave;
    /**
   * 是否计算调休
   */
    private Integer isCompensatory;
    /**
   * 是否启用白名单
   */
    private Integer isWhiteList;
    /**
     * 是否演练模式：0-正式删除，1-只记录判定不执行删除。
     */
    private Integer dryRun;
    /**
   * 空白字段1
   */
    private String blank1;
    /**
   * 园区id
   */
    private Integer parkId;
    /**
   * 创建时间
   */
	@TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    /**
   * 修改时间
   */
	@TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;

}
