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
 * @date 2021-09-14 20:14:59
 */
@Data
@TableName("smt_dormitory_person")
@EqualsAndHashCode(callSuper = true)
public class SmtDormitoryPerson extends Model<SmtDormitoryPerson> {
private static final long serialVersionUID = 1L;

    /**
   * ID
   */
	@TableId(value = "id", type = IdType.ID_WORKER)
    private Long id;
    /**
   * 配置表ID
   */
    private Long configId;
    /**
   * 用户账号
   */
    private String account;
    /**
   * 用户名
   */
    private String name;
    /**
   * 关联宿舍楼
   */
    private String dormitoryIds;
    /**
   * 园区ID
   */
    private Integer parkId;

}
