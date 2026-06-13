package com.tce.smart.platform.core.vo;
import com.tce.smart.common.core.vo.BaseVO;

import lombok.Data;

/**
 * 查询访客分析数据
 *
 * @author 梁圆
 * @date 2019-04-13 18:18:20
 */
@Data
public class SearchVisitorDeviceVO extends BaseVO{

	private static final long serialVersionUID = 1L;


	//设备id
	private String deviceId;

	//设备名称
    private String deviceName;
    /**
     * 进厂描述
     */
    private Integer eventType;
    /**
     * 进厂数据
     */
    private Integer eventCount;
    /**
     * 进厂描述
     */
    private String eventTypeDesc;


}
