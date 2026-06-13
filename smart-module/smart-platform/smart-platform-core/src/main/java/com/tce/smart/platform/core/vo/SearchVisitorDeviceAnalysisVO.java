package com.tce.smart.platform.core.vo;
import java.util.Date;

import com.tce.smart.common.core.vo.BaseVO;

import lombok.Data;

/**
 * 查询访客分析数据
 *
 * @author 梁圆
 * @date 2019-04-13 18:18:20
 */
@Data
public class SearchVisitorDeviceAnalysisVO extends BaseVO{

	private static final long serialVersionUID = 1L;


	//设备id
	private String deviceId;

	//设备名称
    private String deviceName;
    /**
     * 人员姓名
     */
    private String personName;
    /**
     * 人员图片的url
     */
    private String personUrl;
    /**
     * 抓拍图片的url
     */
    private String snapPhotoUrl;

    /**
     * 单位名称
     */
    private String company;
    /**
     * 身份
     */
    private Integer personType;
    /**
     * 身份描述
     */
    private String personTypeDesc;

    /**
     * 进出门类型
     */
    private Integer eventType;

    /**
     * 进厂描述
     */
    private String eventTypeDesc;

    /**
     * 抓拍时间
     */
    private Date snapTime;
}
