package com.tce.smart.platform.api.dto.resp;

import com.tce.smart.common.core.vo.BaseVO;
import com.tce.smart.platform.api.dto.SmtVisitorProcessRecordDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;
import java.util.List;

/**
 * 访客表
 *
 * @author liangyuan
 * @date 2019-04-11 15:57:18
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SearchAppVisitorDetailRespDTO extends BaseVO {
private static final long serialVersionUID = 8072182079911911218L;

/**
 * 访客的信息
 */
   private Integer parkId;
   private String parkName;

   private Long visitorId;

   private String visitorName;

   private String visitorPhotoId;
   private String visitorPhoto;

   private String visitorPhone;

   private String vehiclePlate;

   private String company;
    /**
     * 事由
     */
    private Integer cause;
    private String  causeDesc;
    /**
     * 状态
     */
    private Integer status;
    private String statusDesc;
    /**
   *
   */
    private Date startTime;
    /**
   *
   */
    private Date endTime;
    /**
     *
     */
    private String receptionistName;
    /**
   *
   */
    private String receptionistPhone;
    /**
     * 身份证号
     */
    private String certNo;


	/**
	 * 访客身份证正面照
	 */
	private String visitorFrontPhoto;

	/**
	 * 身份证背面照
	 */
	private String visitorBackPhoto;


	private String remark;


    /**
     * 访客的跟随人员信息
     */
    private List<GetSmtFellowVisitorRespDTO> fellowVisitorList;

    /**
     * 访客审批流程
     */
    private List<SmtVisitorProcessRecordDTO> processList;


	/**
	 * 行程二维码
	 */
	private String tripCode;

	/**
	 * 健康二维码
	 */
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
	private String processId;


}
