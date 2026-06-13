package com.tce.smart.temporary.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

/**
 * 员工亲属表
 * @author QIPEI
 *
 */
@Data
@TableName("EBG_eJAvoidance_register")
public class EbgeJavoidanceRegister {

    @TableId(value = "ID", type = IdType.AUTO)
    private Integer id;

	private Integer ezid;

	private Integer EID;

	private Integer status;

	private String Badge;

	private String Name;

	private Integer Compid;

	private Integer Depid;

	private String Jobid;

    @TableField("Jchenid")
	private Integer JchenID;

    @TableField("Relativesgx")
	private Integer RelativesGX;

    @TableField("Relativesbadge")
	private String RelativesBadge;

    @TableField("Relativescompid")
	private Integer RelativesCompid;

    @TableField("Relativesdepid")
	private Integer RelativesDepid;

    @TableField("Relativesjobid")
	private Integer RelativesJobid;

    @TableField("Rejchenid")
	private Integer REJchenID;

	private String remark;

	private Integer seqid;

	private String qsgx;

}
