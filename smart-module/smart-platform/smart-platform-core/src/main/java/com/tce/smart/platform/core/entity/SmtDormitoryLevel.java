package com.tce.smart.platform.core.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 宿舍职层关联表
 *
 * @author 齐佩
 * @date 2019-04-18 14:47:57
 */
@Data
@TableName("smt_dormitory_level")
@EqualsAndHashCode(callSuper = true)
public class SmtDormitoryLevel extends Model<SmtDormitoryLevel> {
private static final long serialVersionUID = 1L;

    /**
   *
   */
    @TableId
    private Integer id;
    /**
   * 职层id
   */
    private String jcheId;

    /**
     * 职层name
     */
    private String jcheName;
    /**
   * 宿舍类型id
   */
    private Integer dormitoryTypeId;

}
