package com.tce.smart.data.api.model;

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
public class YsJqun extends Model<YsJqun> {

    private static final long serialVersionUID = 1L;

    @TableField("JQunID")
    private Integer JQunID;
    @TableField("JqunName")
    private String JqunName;
}
