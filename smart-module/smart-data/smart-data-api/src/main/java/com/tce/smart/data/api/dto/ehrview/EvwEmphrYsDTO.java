package com.tce.smart.data.api.dto.ehrview;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
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
public class EvwEmphrYsDTO implements Serializable {

    private static final long serialVersionUID = 5640318205109289652L;

    private String Badge;
    private String Name;
    private Integer CompID;
    private String compname;
    private Integer depid;
    private String depname;
    private String jobid;
    private String jobname;
    private String ReportTo;
    private Integer Status;
    private String statusName;
    private Integer EmpType;
    private String emptypeName;
    private Integer JchenID;
    private String JchenName;
    private String flcj;
    private Date joindate;
    private Date LeaDate;
    private Integer age;
    private Date BirthDay;
    private String certno;
    private Integer Gender;
    private String genderName;
    private String mobile;
    private String tel;
    private String email;
    private String emergencyname;
    private Integer relation;
    private String relationName;
    private String telephone;
    private Date altertime;
    private Integer ezid;
    private Integer EID;
	private Integer UserId;
    private Integer PZID;
    private String e_salaryType;
    private String salarytypeName;
	private String residentaddress;
	private String nation;

}
