package com.tce.smart.platform.core.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * 员工表
 *
 * @author 齐佩
 * @date 2019-04-13 18:18:42
 */
@Data
@TableName("smt_staff")
@EqualsAndHashCode(callSuper = true)
public class SmtStaff extends Model<SmtStaff> {
	private static final long serialVersionUID = 1L;
	/**
	*
	*/
	@TableId(value = "id", type = IdType.ID_WORKER)
	private Long id;
	/**
	 * hr入职员工表  关联此表
	 */
	private Integer seqId;
	/**
	 * 员工姓名
	 */
	private String name;
	/**
	 * 员工工号
	 */
	private String badge;
	/**
	 * 岗位ID
	 */
	private String jobId;
	/**
	 * 岗位名称
	 */
	private String jobName;
	/**
	 * BUId
	 */
	private String compId;
	/**
	 * buname
	 */
	private String compName;

	/**
	 * 中心
	 */
	private String depAbbr;

	/**
	 * 部门ID
	 */
	private String depId;
	/**
	 * 部门名称
	 */
	private String depName;
	/**
	 * 职层ID
	 */
	private String jcheId;
	/**
	 * 职层名称
	 */
	private String jcheName;

	/**
	 * 福利层次
	 */
	@NotBlank(message = "职层层次不能为空")
	private String welfareLevel;
	/**
	 * 身份证号
	 */
	private String certno;
	/**
	 * 男/女，应聘填写时OCR识别出来身份证号码，最后一位顺奇数分配为男性，偶数分配为女性
	 */
	private Integer sex;
	/**
	 * 2位数字，根据身份证号码计算年龄
	 */
	private Integer age;
	/**
	 * 出生年月
	 */
	private String birth;
	/**
	 * 电话
	 */
	private String phone;
	/**
	 * 微信号
	 */
	private String wechat;
	/**
	 * 邮箱
	 */
	private String email;

	/**
	 * 家庭住址
	 */
	private String homeAddress;
	/**
	 * 居住地址
	 */
	private String liveAddress;
	/**
	 * 证件照片id
	 */
	private String certnoPicId;
	/**
	 * 人脸照片id
	 */
	private String facePicId;
	/**
	 * 员工状态 0-已离职 1-在职
	 */
	private Integer status;
	/**
	 * 入职时间
	 */
	private Date createTime;

	 /**
     * 住宿状态  0-未住宿  1-内宿  2-外宿
     */
    private Integer dormitoryStatus;

	/**
	 * 签证机关
	 */
	private String police;

	/**
	 * 证件有效开始时间
	 */
	private Date validDateFm;

	/**
	 * 证件有效结束时间
	 */
	private Date validDate;

	/**
	 * 管理hr员工表EID
	 */
	private Integer eId;

	/**
	 * 员工类型
	 */
	private Integer empType;

	/**
	 * 直接上级领导员工号
	 */
	private String reportTo;

	/**
	 * 应聘id
	 */
	private Long applicationId;

	private Integer pzid;

	private String dispatch;

	/**
	 * 民族
	 */
	private String nation;

	/**
	 * 户籍
	 */
	private String residentaddress;

	/**
	 * 离职日期
	 */
	private Date LeaDate;

	/**
	 * 离职类型
	 */
	private String leaType;

	/**
	 * 派遣公司
	 */
	private String pqcompany;
}
