package com.tce.smart.platform.core.entity.dormitoryconfig;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 *
 *
 * @author fushiping
 * @date 2021-09-14 20:14:53
 */
@Data
@TableName("smt_dormitory_config")
@EqualsAndHashCode(callSuper = true)
public class SmtDormitoryConfig extends Model<SmtDormitoryConfig> {
private static final long serialVersionUID = 1L;

    /**
   * ID
   */
	@TableId(value = "id", type = IdType.ID_WORKER)
    private Long id;
    /**
   * 园区id
   */
    private Integer parkId;
    /**
   * 园区名
   */
    private String parkName;
    /**
   * 关联BU
   */
    private String relationBus;
    /**
   * 创建时间
   */
    private LocalDateTime createTime;

}
