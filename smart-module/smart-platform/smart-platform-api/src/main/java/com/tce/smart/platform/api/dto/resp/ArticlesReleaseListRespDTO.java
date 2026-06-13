package com.tce.smart.platform.api.dto.resp;

import cn.afterturn.easypoi.excel.annotation.Excel;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.tce.smart.common.core.dto.BaseDTO;
import com.tce.smart.platform.api.dto.resp.approval.ApprovalProcessRecordReqDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * @Auther: guohongtai
 * @Date: 2020-07-23 17:36
 */
@Data
public class ArticlesReleaseListRespDTO extends BaseDTO {
	@JsonSerialize(using = ToStringSerializer.class)
	private Long id;
	@Excel(name = "工号", orderNum = "1")
	private String badge;
	private Integer dormitoryId;
	private String dormitoryName;
	private Integer floorId;
	private Integer floorName;
	private Integer roomId;
	private Integer roomName;
	private Integer bedId;
	private Integer bedName;
	@Excel(name = "BU", orderNum = "3")
	private String compName;
	@Excel(name = "部门", orderNum = "4")
	private String deptName;
	private String parkName;
	@Excel(name = "申请人", orderNum = "2")
	private String name;
	private String facePic;
	private String qrCodePic;
	private String phone;
	private String articlesTypeName;
	private String articlesDesc;
	private String carrier;
	private Date plannedDepartureTime;
	private String licensePlate;
	private String remarks;
	private Integer status;
	private String statusName;
	private String approver;
	private Date approveTime;
	private String securityStaff;
	private Date departureTime;
	private String oneImg;
	private String twoImg;
	private String threeImg;
	@Excel(name = "创建时间", exportFormat = "yyyy-MM-dd HH:mm:ss", orderNum = "7")
	private LocalDateTime createTime;
	private String remark;
	private Boolean expire;
	@ApiModelProperty(value = "放行事项")
	@Excel(name = "放行事项", orderNum = "5")
	private String releaseItemDesc;
	@ApiModelProperty(value = "放行状态")
	private String releaseStatus;
	@ApiModelProperty(value = "OA节点")
	private String oaNode;
	@ApiModelProperty(value = "返厂确认状态")
	private String backStatus;
	private String roomInfo;
	@ApiModelProperty(value = "流程编号")
	@Excel(name = "流程编号", orderNum = "6")
	private String processId;

	@ApiModelProperty("审批流程")
	List<ApprovalProcessRecordReqDTO> approvalProcess;
}
