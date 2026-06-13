package com.tce.smart.platform.core.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 园区BU关系表
 *
 * @author mckaywu
 * @date 2019-11-20 10:35:16
 */
@Data
@TableName("smt_park_bu")
@EqualsAndHashCode(callSuper = true)
public class SmtParkBu extends Model<SmtParkBu> {
private static final long serialVersionUID = 1L;

    /**
   * 主键ID
   */
    @TableId
    private Integer id;
    /**
   * 园区编号
   */
    private Integer parkId;
    /**
   * BU编号
   */
    private String compId;
	/**
	 * BU名称
	 */
	@TableField(exist = false)
	private String compName;
    /**
   * 创建时间
   */
    private LocalDateTime createTime;

}
