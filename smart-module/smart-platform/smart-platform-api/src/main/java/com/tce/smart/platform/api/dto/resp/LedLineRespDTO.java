package com.tce.smart.platform.api.dto.resp;

import com.tce.smart.common.core.vo.BaseVO;
import lombok.Data;

/**
 * LED信息
 * @Author: yangxu.
 * @Description: TODO()
 * @Date:Created in 2019/4/18 .
 * @Modified By:
 */
@Data
public class LedLineRespDTO extends BaseVO {


    /**
     * 设备编号【必选】
     */
    private String deviceCode;

    /**
     * 显示场景 0：正常场景；1：有权限过车场景；2：无权限过车场景
     */
    private Integer displayScene;

    /**
     * 语音内容
     */
    private String soundText;


    /**
     * 第一行
     */
    private LedAreaRespDTO line1;
	/**
	 * 第二行
	 */
	private LedAreaRespDTO line2;
	/**
	 * 第三行
	 */
	private LedAreaRespDTO line3;
	/**
	 * 第四行
	 */
	private LedAreaRespDTO line4;

}
