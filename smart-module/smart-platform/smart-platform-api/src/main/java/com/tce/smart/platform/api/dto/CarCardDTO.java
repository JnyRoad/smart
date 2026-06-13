package com.tce.smart.platform.api.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 车辆卡片信息
 * @Author: yangxu.
 * @Description: TODO()
 * @Date:Created in 2019/4/18 .
 * @Modified By:
 */
@Data
public class CarCardDTO implements Serializable {

    private static final long serialVersionUID = -32294122564126855L;
    /**
     * 设备编号【必选】
     */
    private String deviceCode;

    /**
     * 卡片编号【必选,纯数字且小于32位】
     */
    private String cardNo;

    /**
     * 卡片类型【必选】0-临时卡；1-固定卡；
     */
    private Integer cardType;

    /**
     * 车牌号【必选】
     */
    private String plateLicence;

    /**
     * 有效期【可选】
     */
    private CarCardValid validTime;

    @Data
    public static class CarCardValid implements Serializable {

        private static final long serialVersionUID = 1448655126653102478L;
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
