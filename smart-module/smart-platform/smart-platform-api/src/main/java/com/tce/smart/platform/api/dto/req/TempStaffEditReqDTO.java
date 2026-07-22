package com.tce.smart.platform.api.dto.req;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.tce.smart.common.core.dto.BaseDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.util.Date;
import java.util.List;

/**
 *
 * @author
 * @date 2019-04-15 11:34:58
 */
@Data
public class TempStaffEditReqDTO extends BaseDTO {

	@ApiModelProperty("主键")
	@JsonSerialize(using = ToStringSerializer.class)
	private Long id;
	/**
	 * 员工姓名
	 */
	@ApiModelProperty("员工姓名")
	private String name;

	@ApiModelProperty("手机号")
	private String phone;

	@ApiModelProperty("员工工号")
	private String badge;
	/**
	 * 身份证号
	 */
	@ApiModelProperty("身份证号")
	private String certno;
	/**
	 * 0-男 1-女
	 */
	@ApiModelProperty("性别")
	private Integer sex;
	/**
	 * 岗位名称
	 */
	@ApiModelProperty("岗位名称")
	private String jobName;
	/**
	 * 部门ID
	 */
	@ApiModelProperty("部门ID")
	private Long depId;
	/**
	 * 部门名称
	 */
	@ApiModelProperty("部门名称")
	private String depName;
	/**
	 * 职层ID
	 */
	@ApiModelProperty("职层ID")
	private String jcheId;
	/**
	 * 职层名称
	 */
	@ApiModelProperty("职层名称")
	private String jcheName;

	@ApiModelProperty("人脸照片")
	private String faceImg;

	@ApiModelProperty("app权限")
	private Integer[] appAuth;

	@ApiModelProperty("员工状态")
	private Integer status;

	@ApiModelProperty("是否上传人脸照片")
	private Boolean isFace;

	@ApiModelProperty("入职时间")
	private String entryTime;

	@ApiModelProperty("派遣渠道")
	private String dispatch;

	@ApiModelProperty("企业类型 1.外协单位 2.派遣工")
	private Integer compType;

	private List<String> ids;

	@ApiModelProperty("查询工号列表")
	private String badges;

	/**
	 * 临时人员资料会用于身份匹配和 ISC 下发，保存前统一去除所有文本字段的首尾空白。
	 */
	public void trimTextFields() {
		name = trim(name);
		phone = trim(phone);
		badge = trim(badge);
		certno = trim(certno);
		jobName = trim(jobName);
		depName = trim(depName);
		jcheId = trim(jcheId);
		jcheName = trim(jcheName);
		faceImg = trim(faceImg);
		entryTime = trim(entryTime);
		dispatch = trim(dispatch);
		badges = trim(badges);
		if (ids != null) {
			ids.replaceAll(this::trim);
		}
	}

	private String trim(String value) {
		return value == null ? null : value.trim();
	}
}
