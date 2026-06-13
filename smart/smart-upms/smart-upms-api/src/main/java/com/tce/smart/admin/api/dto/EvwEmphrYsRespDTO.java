package com.tce.smart.admin.api.dto;

import com.tce.smart.common.core.constant.enums.SexType;
import com.tce.smart.common.core.vo.BaseVO;
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
public class EvwEmphrYsRespDTO extends BaseVO {

    private String badge;
    private String name;
    private Integer compID;
    private String compname;
    private Integer depid;
    private String depname;
    private String jobid;
    private String jobname;
    private String reportTo;
    private Integer status;
    private String statusName;
    private Integer empType;
    private String emptypeName;
    private Integer jchenID;
    private String jchenName;
    private Date joindate;

    /**
	 * 修改日期
	 */
	private Date alterTime;

	/**
	 * 薪资区域
	 */
	private Integer pzid;
	/**
	 * 人事区域
	 */
	private Integer ezid;

	private Integer userid;

	/**
	 * 福利层次
	 */
	private String flcj;
	private String residentaddress;
	private String nation;
	/**
	 * 身份证号
	 */
	private String certno;
	/**
	 * 男/女，应聘填写时OCR识别出来身份证号码，最后一位顺奇数分配为男性，偶数分配为女性
	 */
	private Integer gender;
	/**
	 * 男/女，应聘填写时OCR识别出来身份证号码，最后一位顺奇数分配为男性，偶数分配为女性
	 */
	private String genderName;
	/**
	 * 2位数字，根据身份证号码计算年龄
	 */
	private Integer age;
	/**
	 * 出生年月
	 */
	private Date birthDay;
	/**
	 * 电话
	 */
	private String phone;
	/**
	 * 邮箱
	 */
	private String email;

	/**
	 * 关联hr员工表EID
	 */
	private Integer eId;


    private String salaryType;

	private Date LeaDate;


    private String salarytypeName;
}
