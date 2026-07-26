package com.tce.smart.app.vo.fore;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tce.smart.common.core.vo.BaseVO;
import com.tce.smart.platform.api.dto.SmtVisitorProcessRecordDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;
import java.util.List;

/**
 * 访客详情信息VO
 *
 * @author ly
 * @date 2019-05-10 16:11:13
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class VisitorDetailVo extends BaseVO {

	/**
	 * 序列号
	 */
	private static final long serialVersionUID = 5362758608679031031L;

	/**
	 * 园区Id
	 */
	@ApiModelProperty(value = "园区Id",required = true)
	private Integer parkId;

	/**
	 * 来访园区
	 */
	@ApiModelProperty(value = "来访园区",required = true)
	private String parkName;

	/**
	 * 访客姓名
	 */
	@ApiModelProperty(value = "访客姓名",required = true)
	private String visitorName;

	/**
	 * 访客头像
	 */
	@ApiModelProperty(value = "来访访客头像",required = true)
	private String visitorPhoto;

	/**
	 * 访客手机号
	 */
	@ApiModelProperty(value = "访客手机号",required = true)
	private String visitorMobile;

	/**
	 * 访客单位
	 */
	@ApiModelProperty(value = "访客单位",required = true)
	private String visitorCompany;

	/**
	 * 来访事由
	 */
	@ApiModelProperty(value = "来访事由",required = true)
	private String visitReason;

	/**
	 * 状态
	 */
	@ApiModelProperty(value = "状态",required = true)
    private Integer visitState;

    /**
     * 访客身份证号
     */
	@ApiModelProperty(value = "访客身份证号",required = true)
	@JsonIgnore
    private String visitorCertNo;

	/**
	 * 车牌号
	 */
	@ApiModelProperty(value = "车牌号",required = true)
	private String plateNumber;

	/**
	 * 被访人姓名
	 */
	@ApiModelProperty(value = "被访人姓名",required = true)
	private String employeeName;

	/**
	 * 被访人手机号
	 */
	@ApiModelProperty(value = "被访人手机号",required = true)
	private String employeeMobile;

	/**
	 * 预约来访时间
	 */
	@ApiModelProperty(value = "预约来访时间",required = true)
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm")
	private Date startTime;

	/**
	 * 预约离开时间
	 */
	@ApiModelProperty(value = "预约离开时间",required = true)
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm")
	private Date endTime;

	/**
	 * 访客身份证正面照
	 */
	@ApiModelProperty(value = "访客身份证正面照",required = true)
	@JsonIgnore
	private String visitorFrontPhoto;

	/**
	 * 身份证背面照
	 */
	@ApiModelProperty(value = "身份证背面照",required = true)
	@JsonIgnore
	private String visitorBackPhoto;

    /**
     * 访客的跟随人员信息
     */
	@ApiModelProperty(value = "访客的跟随人员信息",required = true)
    private List<MemberDetailVo> member;

    /**
     * 访客审批流程
     */
	@ApiModelProperty(value = "访客的跟随人员信息",required = true)
	@JsonIgnore
    private List<SmtVisitorProcessRecordDTO> processList;

	/**
	 * 行程二维码
	 */
	@JsonIgnore
	private String tripCode;

	/**
	 * 健康二维码
	 */
	@JsonIgnore
	private String healthcode;

	/**
	 *携带物品
	 */
	private Integer carryThing;

	/**
	 *携带物品
	 */
	private Integer carryThingDesc;

	/**
	 * 流程编号
	 */
	@JsonIgnore
	private String processId;


}
