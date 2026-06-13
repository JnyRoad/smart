package com.tce.smart.data.api.dto.ehrview.resp;

import com.baomidou.mybatisplus.annotation.TableField;
import com.tce.smart.common.core.constant.enums.SexType;
import com.tce.smart.common.core.vo.BaseVO;
import com.tce.smart.data.api.dto.ehrview.EvwEmphrYsDTO;
import com.tce.smart.tool.enums.StaffStatusEnum;
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

	public EvwEmphrYsRespDTO change(EvwEmphrYsDTO evwEmphrYs){
		this.setFlcj(evwEmphrYs.getFlcj());
		this.setCertno(evwEmphrYs.getCertno());
		if(evwEmphrYs.getGender() == 1){
			this.setGender(SexType.MAN.getCode());
		}else if(evwEmphrYs.getGender() == 2){
			this.setGender(SexType.WOMAN.getCode());
		}else {
			this.setGender(SexType.UNKNOWN.getCode());
		}
		this.setGender(evwEmphrYs.getGender());
		this.setGenderName(evwEmphrYs.getGenderName());
		this.setAge(evwEmphrYs.getAge());
		this.setBirthDay(evwEmphrYs.getBirthDay());
		this.setPhone(evwEmphrYs.getTelephone());
		this.setEmail(evwEmphrYs.getEmail());
		this.setAlterTime(evwEmphrYs.getAltertime());
		this.setEzid(evwEmphrYs.getEzid());
		this.setPzid(evwEmphrYs.getPZID());

		this.setBadge(evwEmphrYs.getBadge());
		this.setName(evwEmphrYs.getName());
		this.setCompID(evwEmphrYs.getCompID());
		this.setCompname(evwEmphrYs.getCompname());
		this.setDepid(evwEmphrYs.getDepid());
		this.setDepname(evwEmphrYs.getDepname());
		this.setJobid(evwEmphrYs.getJobid());
		this.setJobname(evwEmphrYs.getJobname());
		this.setReportTo(evwEmphrYs.getReportTo());
		this.setStatus(StaffStatusEnum.changeStaffStatus(evwEmphrYs.getStatus()));
		this.setStatusName(evwEmphrYs.getStatusName());
		this.setEmpType(evwEmphrYs.getEmpType());
		this.setEmptypeName(evwEmphrYs.getEmptypeName());
		this.setJchenID(evwEmphrYs.getJchenID());
		this.setJchenName(evwEmphrYs.getJchenName());
		this.setJoindate(evwEmphrYs.getJoindate());
		this.setEId(evwEmphrYs.getEID());
		this.setSalaryType(evwEmphrYs.getE_salaryType());
		this.setSalarytypeName(evwEmphrYs.getSalarytypeName());
		this.setUserid(evwEmphrYs.getUserId());
		return this;
	}
}
