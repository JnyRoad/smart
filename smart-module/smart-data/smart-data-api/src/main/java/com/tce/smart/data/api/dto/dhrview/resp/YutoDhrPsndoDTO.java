package com.tce.smart.data.api.dto.dhrview.resp;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author wuling
 * @since 2021-05-27
 */
@Data
public class YutoDhrPsndoDTO implements Serializable {
    private static final long serialVersionUID = 5640318205109289652L;

	/**
	 * 工号
	 */
	private String code;

	/**
	 * 姓名
	 */
	private String name;

	/**
	 * BU编码
	 */
	private Integer pkOrg;

	/**
	 * BU名称
	 */
	private String buName;

	/**
	 * 部门编码
	 */
	private Integer pkDept;

	/**
	 * 中心
	 */
	private String depone;

	/**
	 * 部门名称
	 */
	private String depName;


	/**
	 * 部门名称
	 */
	private String deptwe;

	/**
	 * 岗位编码
	 */
	private String pkPost;

	/**
	 * 岗位名称
	 */
	private String postName;

	/**
	 * 直接上级工号
	 */
	private String jobglbdef18;


	/**
	 *  * 101-总裁层，102-副总裁层，103-总监层，104-总经理层，105-经理层，
	 * 	 * 106-课长，107-班组长，108-职层，109-员工层，110-技工层
	 */
	private String jchen;

	/**
	 * 福利层级
	 * 101-A,102-B1,103-B2,104-C,105-D,106-E,107-F,108-G,109-H
	 */
	private String jobglbdef21;

	/**
	 * 员工状态编码
	 * 1-在职/2-试用/3-实习/4-离职
	 */
	private Integer jobglbdef1;

	/**
	 * 员工类型编码
	 * 01-正式工，06-劳务用工，03-劳务派遣工，07-退休返聘，05-实习生，02-裕备生，04-自招挂派遣
	 */
	private String psntype;

	/**
	 * 计薪类型编码
	 * A1-计时,A2-计件,A3-月薪,A4-年薪
	 */
	private Integer jobglbdef12;

	/**
	 * 入职日期
	 */
	private Date glbdef7;

	/**
	 * 证件号码
	 */
	private String glbdef2;

	/**
	 * 性别编码
	 * 1-男，2-女
	 */
	private Integer sex;

	/**
	 * 手机号
	 */
	private String mobile;

	/**
	 * 邮箱
	 */
	private String email;

	private String nowhometown;

	private String gender;

	private String enddate;

	/**
	 * 离职类型
	 */
	private String leatype;

	private String censusaddr;

	/**
	 * 员工性质
	 */
	private String jobglbdef22;

	/**
	 * 派遣公司
	 */
	private String pqcompany;

}
