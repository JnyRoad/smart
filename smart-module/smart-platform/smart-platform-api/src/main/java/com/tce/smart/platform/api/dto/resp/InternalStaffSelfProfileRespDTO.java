package com.tce.smart.platform.api.dto.resp;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 员工本人资料页使用的内部投影。
 *
 * 该对象只能由已认证用户对应的 App 服务端流程消费，不能复用于管理端或通用员工查询。
 */
@Data
@ApiModel("内部员工本人资料响应")
public class InternalStaffSelfProfileRespDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	private String badge;
	private String name;
	private String phone;
	private Integer sex;
	private String compName;
	private String depName;
	private String jobName;
	private String jcheName;
	private String welfareLevel;
	private String facePicId;
	private Date createTime;
	private String certno;
	private String email;
	private Integer empType;
	private String parkName;
	private Integer dormitoryState;
	private String dormitoryStateDesc;
	private Integer applyState;
	private String applyStateDesc;
	private Integer vehicleState;
	private String vehicleStateDesc;
	private Integer status;
	private String statusDes;
	private String empTypeDes;
	private String facePic;
	@ApiModelProperty("紧急联系人关系编码")
	private String emergencyRelation;
	private String emergencyName;
	private String emergencyPhone;
}
