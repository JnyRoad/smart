package com.tce.smart.platform.core.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 园区车辆入园职层表
 *
 * @author mckaywu
 * @date 2019-11-20 10:36:48
 */
@Data
@TableName("smt_park_vehicle_level")
@EqualsAndHashCode(callSuper = true)
public class SmtParkVehicleLevel extends Model<SmtParkVehicleLevel> {
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
   * 职层编号
   */
    private String jcheId;
	/**
	 * 职层描述
	 */
	@TableField(exist = false)
	private String jcheDesc;
    /**
   * 创建时间
   */
    private LocalDateTime createTime;

}
