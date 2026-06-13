package com.tce.smart.ehrview.core.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;

import java.util.Date;

/**
 * <p>
 *
 * </p>
 *
 * @author WangJinbo123
 * @since 2019-05-03
 */
@Data
@TableName("evw_empHR_ys")
public class EvwEmphrYs extends Model<EvwEmphrYs> {

    private static final long serialVersionUID = 1L;

    private String Badge;
    private String Name;
    @TableField("CompID")
    private Integer CompID;
    private String compname;
    private Integer depid;
    private String depname;
    private String jobid;
    private String jobname;
    @TableField("reportto")
    private String ReportTo;
    private Integer Status;
    @TableField("statusName")
    private String statusName;
    @TableField("EmpType")
    private Integer EmpType;
    @TableField("emptypeName")
    private String emptypeName;
    @TableField("JchenID")
    private Integer JchenID;
    @TableField("JchenName")
    private String JchenName;
    @TableField("FLCJ")
    private String flcj;
    private Date joindate;
    @TableField("LeaDate")
    private Date LeaDate;
    private Integer age;
    @TableField("BirthDay")
    private Date BirthDay;
    private String certno;
    private Integer Gender;
    @TableField("genderName")
    private String genderName;
    private String mobile;
    @TableField("TEL")
    private String tel;
    @TableField("EMAIL")
    private String email;
    @TableField("EMERGENCYNAME")
    private String emergencyname;
    @TableField("RELATION")
    private Integer relation;
    @TableField("relationName")
    private String relationName;
    @TableField("TELEPHONE")
    private String telephone;
    @TableField("alterTime")
    private Date altertime;
    @TableField("ezid")
    private Integer ezid;
    @TableField("eid")
    private Integer EID;
	@TableField("userid")
	private Integer UserId;
    @TableField("pzid")
    private Integer PZID;
    @TableField("e_salarytype")
    private String e_salaryType;
    @TableField("salarytypename")
    private String salarytypeName;
    @TableField("isblacklist")
    private String IsBlackList;
	@TableField(exist = false)
    private String depAbbr;
	@TableField("residentaddress")
	private String residentaddress;
	@TableField("nation")
	private String nation;

	@TableField(exist = false)
	private String leaType;

	/**
	 * 派遣公司
	 */
	@TableField(exist = false)
	private String pqcompany;

}
