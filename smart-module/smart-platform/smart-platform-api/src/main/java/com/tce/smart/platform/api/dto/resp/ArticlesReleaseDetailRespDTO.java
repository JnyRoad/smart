package com.tce.smart.platform.api.dto.resp;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.tce.smart.common.core.dto.BaseDTO;
import com.tce.smart.platform.api.dto.req.ReleaseApplyPersonDetail;
import com.tce.smart.platform.api.dto.req.ReleaseApplyThingDetail;
import com.tce.smart.platform.api.dto.resp.approval.ApprovalProcessRecordReqDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * 物品放行详情
 * @Auther: guohongtai
 * @Date: 2020-07-23 17:36
 */
@Data
public class ArticlesReleaseDetailRespDTO extends BaseDTO {
	@JsonSerialize(using = ToStringSerializer.class)
	private Long id;
	private String badge;
	@ApiModelProperty(value = "申请人")
	private String name;
	private String compName;
	@ApiModelProperty(value = "申请部门")
	private String deptName;
	private String parkName;
	private Integer dormitoryId;
	private String dormitoryName;
	private Integer floorId;
	private String floorName;
	private Integer roomId;
	private String roomName;
	private Integer bedId;
	private Integer bedName;
	private String qrCodePic;
	private String facePic;
	private String phone;
	private String articlesTypeName;
	private Integer articlesType;
	private String articlesDesc;
	private String carrier;
	private Date plannedDepartureTime;
	private String licensePlate;
	private String remarks;
	private Integer status;
	private String statusName;
	private String oneImg;
	private String twoImg;
	private String threeImg;
	private Boolean expire;
	@ApiModelProperty(value = "申请时间")
	private LocalDateTime createTime;
	private String securityStaff;
	private Date departureTime;
	private String remark;
	@ApiModelProperty(value = "保安放行时，是否需要上传图片0:是,1:否")
	private Integer isUploadImg;
	@ApiModelProperty(value = "办公区物品放行数据")
	private ReleaseApplyMainRespDTO applyMain;
	private List<ReleaseApplyPersonDetail> personDetailList;
	private List<ReleaseApplyThingDetail> thingDetailList;

	@ApiModelProperty("审批流程")
	List<ApprovalProcessRecordReqDTO> approvalProcess;
}
