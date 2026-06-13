package com.tce.smart.ehrview.core.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;

/**
 * <p>
 * <p>
 * </p>
 *
 * @author WangJinbo123
 * @since 2019-05-03
 */
@Data
public class YsJchen extends Model<YsJchen> {

    private static final long serialVersionUID = 1L;

    @TableField("JchenID")
    private Integer jchenId;
    @TableField("JchenName")
    private String jchenName;
}
