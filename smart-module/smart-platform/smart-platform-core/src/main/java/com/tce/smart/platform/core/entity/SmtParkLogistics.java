package com.tce.smart.platform.core.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 园区物流关系表
 *
 * @author mckaywu
 * @date 2019-11-20 10:36:33
 */
@Data
@TableName("smt_park_logistics")
@EqualsAndHashCode(callSuper = true)
public class SmtParkLogistics extends Model<SmtParkLogistics> {
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
   * 物流中心编号
   */
    private String companyId;
	/**
	 * 物流中心名称
	 */
	@TableField(exist = false)
	private String companyName;
    /**
   * 创建时间
   */
    private LocalDateTime createTime;

}
