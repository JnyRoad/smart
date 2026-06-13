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
@TableName("yuto_dhr_dept")
public class YutoDhrDept extends Model<YutoDhrDept> {

    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @TableField("PK_DEPT")
    private Integer pkDept;
    /**
     * 名称
     */
    @TableField("NAME")
    private String name;
    /**
     * 组织ID
     */
    @TableField("PK_ORG")
    private Integer pkOrg;

    /**
     * 部门负责人工号
     */
    @TableField("DIRECTOR")
    private String director;

    /**
     * 部门负责人姓名
     */
    @TableField("DIRECTORNAME")
    private String directorName;

    /**
     * 部门等级: 1-中心, 2-部门, 3-课室, 4-组
     */
    @TableField("DEPTLEVEL")
    private String deptLevel;

    /**
     * 上级部门ID
     */
    @TableField("PK_FATHERORG")
    private Integer pkFatherOrg;

    /**
     * 成本中心编码
     */
    @TableField("GLBDEF3")
    private String glbdef3;

    /**
     * 上级部门ID
     */
    @TableField("ENABLESTATE")
    private Integer enableState;

}
