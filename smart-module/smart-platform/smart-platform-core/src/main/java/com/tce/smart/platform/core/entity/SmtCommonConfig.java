package com.tce.smart.platform.core.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

/**
 * 预约配置表
 *
 * @author fushiping
 * @date 2021-08-13 16:08:16
 */
@Data
@TableName("smt_common_config")
@EqualsAndHashCode(callSuper = true)
public class SmtCommonConfig extends Model<SmtCommonConfig> {
private static final long serialVersionUID = 1L;

    /**
   * ID
   */
	@TableId(value = "id", type = IdType.ID_WORKER)
    private Long id;
    /**
   * 预约类型
   */
    private Integer businessType;
    /**
   * 园区ID
   */
    private Integer parkId;
    /**
   * 配置类型
   */
    private Integer configType;
    /**
   * 配置值 使用 json类型保存
   */
    private String value;
    /**
   * 保留字段
   */
    private String publicField;
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
    /**
   * 是否删除
   */
    private Integer isDelete;

}
