package com.tce.smart.xcc6.core.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 人事信息表
 *
 * @author mkwu
 * @date 2019-07-29
 */
@Data
@EqualsAndHashCode(callSuper=false)
@TableName("RS_Emp")
public class RsEmp extends Model<RsEmp> {

    /**
	 * 序列号
	 */
	private static final long serialVersionUID = 55541266638449276L;

	@TableField("EmpSysID")
    private String EmpSysID;

    @TableField("EmpNo")
    private String EmpNo;

    @TableField("EmpName")
    private String EmpName;

    @TableField("EmpEngName")
    private String EmpEngName;

    @TableField("EmpSexID")
    private Short EmpSexID;

	@TableField("DptSysID")
	private String DptSysID;

    @TableField("EmpIsForeign")
    private Character EmpIsForeign;

    @TableField("EmpIsStopSalary")
    private Character EmpIsStopSalary;

    @TableField("EmpNationality")
    private String EmpNationality;

    @TableField("EmpNation")
    private String EmpNation;

    @TableField("EmpBirthday")
    private Date EmpBirthday;

	@TableField("EmpGrpDate")
	private Date EmpGrpDate;

	@TableField("EmpStatusID")
	private Integer EmpStatusID;

	@TableField("GrdSysID")
	private String GrdSysID;

	@TableField("EduSysID")
	private String EduSysID;

	@TableField("TitSysID")
	private String TitSysID;

	@TableField("PosSysID")
	private String PosSysID;

	@TableField("TypSysID")
	private String TypSysID;

	@TableField("LzxSysID")
	private String LzxSysID;

	@TableField("LzySysID")
	private String LzySysID;

	@TableField("PrvSysID")
	private String PrvSysID;

	@TableField("GzjSysID")
	private String GzjSysID;

	@TableField("GzdSysID")
	private String GzdSysID;

	@TableField("EmpIDType")
	private Integer EmpIDType;

	@TableField("CardTypeID")
	private Integer CardTypeID;

	@TableField("PeriodType")
	private String PeriodType;

	@TableField("EmpIDNo")
	private String EmpIDNo;

	@TableField("EmpLeaveDate")
	private Date EmpLeaveDate;
}
