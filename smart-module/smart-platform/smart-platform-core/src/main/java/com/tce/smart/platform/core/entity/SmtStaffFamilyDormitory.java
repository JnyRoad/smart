package com.tce.smart.platform.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * 员工家属住宿信息表
 *
 * @author wuling
 * @date 2020-12-08 16:43:41
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("SMT_STAFF_FAMILY_DORMITORY")
@EqualsAndHashCode(callSuper = true)
public class SmtStaffFamilyDormitory extends Model<SmtStaffFamilyDormitory> {
	private static final long serialVersionUID = 8574328230919464041L;

    /**
   * 主键
   */
	@TableId(value = "id", type = IdType.ID_WORKER)
    private Long id;

    /**
   * 名称
   */
    private String name;

	/**
	 * 工号
	 */
    private String badge;

    /**
   * 身份证
   */
    private String certno;

    /**
   * 手机号
   */
    private String phone;

    /**
   * 家属关系 1.夫妻 2.直系血亲 3.旁系血亲 4.近姻亲 5.其他
   */
    private Integer relation;

    /**
   * 员工工号
   */
    private String staffBadge;

	/**
	 * 是否删除 0.未删除 1.已删除
	 */
	private Integer delFlag;

    /**
   * 创建时间
   */
    private Date createTime;

}
