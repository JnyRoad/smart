package com.tce.smart.dhrview.core.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @description: DHR员工表试图
 * @date: 2021/5/27 0027 14:39
 * @author: wuling
 * @version: 1.0
 */
@Data
@TableName("YUTO_DHR_PSNDOC")
public class YutoDhrPsndo {

	/**
	 * 用户ID
	 */
	@TableField("USERID")
	private String userId;
	/**
	 * 工号
	 */
	@TableField("CODE")
	private String code;

	/**
	 * 姓名
	 */
	@TableField("NAME")
	private String name;

	/**
	 * BU编码
	 */
	@TableField("PK_ORG")
	private Integer pkOrg;

	/**
	 * BU名称
	 */
	@TableField("BUNAME")
	private String buName;

	/**
	 * 部门编码
	 */
	@TableField("PK_DEPT")
	private Integer pkDept;

	/**
	 * 部门名称
	 */
	@TableField("DEPNAME")
	private String depName;

	/**
	 * 岗位编码
	 */
	@TableField("PK_POST")
	private String pkPost;

	/**
	 * 岗位名称
	 */
	@TableField("POSTNAME")
	private String postName;

	/**
	 * 直接上级工号
	 */
	@TableField("JOBGLBDEF18")
	private String jobglbdef18;

	/**
	 * 员工性质
	 */
	@TableField("JOBGLBDEF22")
	private String jobglbdef22;

	/**
	 * 职层
	 *  * 101-总裁层，102-副总裁层，103-总监层，104-总经理层，105-经理层，
	 * 	 * 106-课长，107-班组长，108-职层，109-员工层，110-技工层
	 */
	@TableField("JCHEN")
	private String jchen;

	/**
	 * 福利层级
	 * 101-A,102-B1,103-B2,104-C,105-D,106-E,107-F,108-G,109-H
	 */
	@TableField("JOBGLBDEF21")
	private String jobglbdef21;

	/**
	 * 是否外宿: Y / N
	 */
	@TableField("glbdef28")
	private String glbdef28;

	/**
	 * 员工状态编码
	 * 1-在职/2-试用/3-实习/4-离职
	 */
	@TableField("JOBGLBDEF1")
	private Integer jobglbdef1;

	/**
	 * 员工类型编码
	 * 01-正式工，06-劳务用工，03-劳务派遣工，07-退休返聘，05-实习生，02-裕备生，04-自招挂派遣
	 */
	@TableField("PSNTYPE")
	private String psntype;

	/**
	 * 计薪类型编码
	 * A1-计时,A2-计件,A3-月薪,A4-年薪
	 */
	@TableField("JOBGLBDEF12")
	private String jobglbdef12;

	/**
	 * 入职日期
	 */
	@TableField("GLBDEF7")
	private String glbdef7;

	/**
	 * 证件号码
	 */
	@TableField("GLBDEF2")
	private String glbdef2;

	/**
	 * 性别编码
	 * 1-男，2-女
	 */
	@TableField("SEX")
	private Integer sex;

	/**
	 * 手机号
	 */
	@TableField("MOBILE")
	private String mobile;

	/**
	 * 邮箱
	 */
	@TableField("EMAIL")
	private String email;

	/**
	 * 籍贯
	 */
	@TableField("NOWHOMETOWN")
	private String nowhometown;

	/**
	 * 民族
	 */
	@TableField("GENDER")
	private String gender;

	/**
	 * 离职日期
	 */
	@TableField("ENDDATE")
	private String enddate;

	/**
	 * 离职类型
	 */
	@TableField("LEATYPE")
	private String leatype;

	/**
	 * 中心
	 */
	@TableField("DEPONE")
	private String depone;


	/**
	 * 部门名称
	 */
	@TableField("DEPTWE")
	private String deptwe;

	/**
	 * 户籍地址
	 */
	@TableField("CENSUSADDR")
	private String censusaddr;

	/**
	 * 派遣公司
	 */
	@TableField("PQCOMPANY")
	private String pqcompany;

}
