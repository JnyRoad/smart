package com.tce.smart.platform.core.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.tce.smart.common.core.vo.BaseVO;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

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
public class EmpHrVO extends BaseVO {
	private static final long serialVersionUID = 1L;

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
	private String depAbbr;
	@JsonFormat(pattern = "yyyy-MM-dd")
	@DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date joindate;

	/**
	 * 福利层次
	 */
	private String flcj;
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
	@JsonFormat(pattern = "yyyy-MM-dd")
	@DateTimeFormat(pattern = "yyyy-MM-dd")
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
	 * 修改时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd")
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private Date altertime;

	private Integer ezid;

	private Integer eid;

	private Integer pzid;
	private String residentaddress;
	private String nation;
	private Date LeaDate;
	private String leaType;
	private String pqcompany;
}
