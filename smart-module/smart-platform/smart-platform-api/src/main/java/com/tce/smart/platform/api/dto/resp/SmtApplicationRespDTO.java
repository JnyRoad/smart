package com.tce.smart.platform.api.dto.resp;

import com.tce.smart.common.core.vo.BaseVO;
import lombok.Data;

import java.util.Date;

/**
 * 应聘表
 *
 * @author 齐佩
 * @date 2019-04-13 18:18:24
 */
@Data
public class SmtApplicationRespDTO extends BaseVO {
	private static final long serialVersionUID = 4492734272100457018L;

	/**
	*
	*/
	private Long id;
	/**
	 * 对应招聘id
	 */
	private Integer recruitId;
	/**
	 * 所属园区id
	 */
	private Integer parkId;
	/**
	 * 应聘者姓名
	 */
	private String name;
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
	 * 民族
	 */
	private String nation;
	/**
	 * 出生年月日
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
	 * 人脸照片URl
	 */
	private String facePicId;
	/**
	 * 应聘状态
		0-已投递1-已拒绝 2-已邀请3-待入职/4待复试，暂定/5-已入职
		默认是0
	 */
	private Integer status;


	/**
	 * 面试时间
	 */
	private Date interviewTime;
	/**
	* 创建时间
	*/
	private Date createTime;

	/**
	 * 投递时间
	 */
	private Date applyDate;
	/**
	 * 是否删除;0：未删；1：已删，默认是0
	 */
	private Integer isDelete;


	/**
	 * 拒绝原因
	 */
	private String refuseReason;

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
	 * 婚姻状况0-未婚 1-已婚 其他-未知
	 */
	private Integer maritalStatus;
}
