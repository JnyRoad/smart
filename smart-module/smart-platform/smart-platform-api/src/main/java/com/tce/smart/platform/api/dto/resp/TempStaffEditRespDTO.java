package com.tce.smart.platform.api.dto.resp;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.tce.smart.common.core.dto.BaseDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 *
 * @author
 * @date 2019-04-15 11:34:58
 */
@Data
public class TempStaffEditRespDTO extends BaseDTO {

	@ApiModelProperty("主键")
	@JsonSerialize(using = ToStringSerializer.class)
	private Long id;
	/**
	 * 员工姓名
	 */
	@ApiModelProperty("员工姓名")
	private String name;

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

	@ApiModelProperty("手机号")
	private String phone;
	/**
	 * 岗位名称
	 */
	@ApiModelProperty("岗位名称")
	private String jobName;

	@ApiModelProperty("BU名称")
	private String compName;
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

	@ApiModelProperty("人脸照片链接")
	private String faceImgUrl;

	@ApiModelProperty("app权限")
	private List<Integer> appAuth;

	@ApiModelProperty("是否上传人脸照片")
	private Boolean isFace;
	/**
	 * 直接上级领导员工号
	 */
	@ApiModelProperty("直接上级工号")
	private String reportTo;

	@ApiModelProperty("直接上级姓名")
	private String reportToName;

	@ApiModelProperty("人员状态")
	private Integer status;

	@ApiModelProperty("入职时间")
	private String entryTime;

	@ApiModelProperty("派遣渠道")
	private String dispatch;
}
