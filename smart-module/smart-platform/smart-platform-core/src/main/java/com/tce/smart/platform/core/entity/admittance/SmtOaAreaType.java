package com.tce.smart.platform.core.entity.admittance;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

/**
 *
 *
 * @author fushiping
 * @date 2021-08-17 17:45:30
 */
@Data
@Builder
@TableName("smt_oa_area_type")
@EqualsAndHashCode(callSuper = true)
public class SmtOaAreaType extends Model<SmtOaAreaType> {
private static final long serialVersionUID = 1L;

    /**
   * id
   */
	@TableId(value = "id", type = IdType.ID_WORKER)
    private Long id;
    /**
   * oa类别名
   */
    private String typeName;
    /**
   * oa类别ID
   */
    private String typeId;

	/**
	 * oa类别值
	 */
	private String typeValue;
    /**
   * 创建时间
   */
	@TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    /**
   * 是否删除
   */
    private Integer isDelete;

	/**
	 * 记录类型
	 */
	private Integer type;

}
