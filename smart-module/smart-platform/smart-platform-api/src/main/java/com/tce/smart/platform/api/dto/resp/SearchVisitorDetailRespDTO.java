package com.tce.smart.platform.api.dto.resp;

import com.tce.smart.common.core.vo.BaseVO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;
import java.util.List;

/**
 * @Title: SearchVisitorDetailRespDTO
 * @Auther: guohongtai
 * @Date: 2020-10-21 20:14
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SearchVisitorDetailRespDTO extends BaseVO {
	private Long id;
	private String remotePath;
	private String visitorName;
	private String visitorPhoto;
	private String visitorPhone;
	private String company;
	private String causeDesc;
	private Date startTime;
	private Date endTime;
	private String parkName;
	private String receptionistName;
	private String receptionistPhone;
	private List<GetSmtFellowVisitorRespDTO> fellowVisitorList;
	private Integer isVip;
	private String smsCode;
	private String qrCode;
	private Integer delFlag;
	private String tripCode;
	private String healthcode;
	private String processId;
}
