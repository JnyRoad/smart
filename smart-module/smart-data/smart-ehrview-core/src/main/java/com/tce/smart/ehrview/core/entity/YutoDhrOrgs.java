package com.tce.smart.ehrview.core.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;

/**
 * <p>
 *
 * </p>
 *
 * @author WangJinbo123
 * @since 2019-05-03
 */
@Data
@TableName("yuto_dhr_orgs")
public class YutoDhrOrgs extends Model<YutoDhrOrgs> {

    /**
     * ID
     */
    @TableField("PK_ORG")
    private Integer pkOrg;
    /**
     * 名称
     */
    @TableField("NAME")
    private String name;
    /**
     * 上级ID
     */
    @TableField("PK_FATHERORG")
    private Integer pkFatherOrg;

    /**
     * 1-未启用, 2-已启用, 3-已废弃
     */
    @TableField("ENABLESTATE")
    private Integer enableState;
}
