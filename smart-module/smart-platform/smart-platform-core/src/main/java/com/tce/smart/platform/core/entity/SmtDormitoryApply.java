package com.tce.smart.platform.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * 内宿申请记录表
 *
 * @author wulign
 * @date 2020-12-29
 */
@Data
@TableName("SMT_DORMITORY_APPLY")
@EqualsAndHashCode(callSuper = true)
public class SmtDormitoryApply extends Model<SmtDormitoryApply> {
	private static final long serialVersionUID = 4212205757050465039L;

    /**
   * 主键
   */
	@TableId(value = "id", type = IdType.ID_WORKER)
    private Long id;

    /**
   * 园区主键
   */
    private Integer parkId;

    /**
   * 员工工号
   */
    private String staffBadge;

    /**
   * 员工姓名
   */
    private String staffName;

    /**
   * 喜好类型 1.下铺 2.上铺
   */
    private Integer likeType;

    /**
   * 申请备注
   */
    private String applyRemark;

	/**
	 * 申请状态 1.申请中 2.申请通过 3.退回
	 */
	private Integer status;

	/**
	 * 结果描述
	 */
	private String resultRemark;

    /**
   * 创建时间
   */
    private Date createTime;

	/**
	 * 修改时间
	 */
	private Date updateTime;

}
