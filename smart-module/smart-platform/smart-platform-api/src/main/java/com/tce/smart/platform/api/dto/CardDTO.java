package com.tce.smart.platform.api.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 人员卡片信息
 * @Author: yangxu.
 * @Description: TODO()
 * @Date:Created in 2019/4/18 .
 * @Modified By:
 */
@Data
public class CardDTO implements Serializable {
    private static final long serialVersionUID = 6688927601908056816L;

    private Integer parkId;
    /**
     * 设备编号【必选】
     */
    private String deviceCode;

    /**
     * 卡片编号【必选,纯数字且小于32位】
     */
    private String cardNo;

	/**
	 * 员工号，暂只支持纯数字的员工号，非纯数字的不下发
	 */
	private Integer employeeNo;

	/**
     * 卡片类型【必选】1-普通卡；2-残疾卡；3-黑名单卡；4-巡逻卡；5-胁迫卡；6-超级卡；7-来宾卡
     */
    private Integer cardType;

	/**
	 * 序号
	 */
	private String serialNo;

	/**
	 * 主键
	 */
	private Integer reqId;


    /**
     * 人脸图片【必选】
     */
    private String faceImage;

    /**
     * 人员名称【必选】
     */
    private String personName;

    /**
     * 有效期【可选】
     */
    private CardValid validTime;

    @Data
    public static class CardValid implements Serializable{

        private static final long serialVersionUID = -6569184417693464518L;
        /**
         * 有效期开始时间【必选】
         */
        private Long startTime;

        /**
         * 有效期结束时间【必选】
         */
        private Long endTime;
    }
}
