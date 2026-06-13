package com.tce.smart.algorithm.api.enums;

import com.tce.smart.algorithm.api.dto.resp.*;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

/**
 * @author wuxinjian
 */

@Getter
@AllArgsConstructor
public enum CardTypeEnum {

    /**
     * 身份证
     */
    ID_CARD_FRONT("2", "身份证正面", IdCardFrontDTO.class),
    /**
     * 身份证
     */
    ID_CARD_BACK("3", "身份证背面", IdCardBackDTO.class),
    /**
     * 临时身份证
     */
	TEMPORARY_ID_CARD("4", "临时身份证", TemporaryCardDTO.class),
    /**
     * 驾照
     */
    DRIVER_CARD("5", "驾照", DriverCardDTO.class),
    /**
     * 护照
     */
    PASSPORT_CARD("13", "护照", PassPortDTO.class),
    /**
     * 港澳内陆通行证
     */
	HK_MACAO_INLAND_PASS_CARD("22", "港澳内陆通行证", HKMacaoInlandPassDTO.class),
    /**
     * 台湾往来大陆通行证
     */
    TAIWAN_MAINLAND_PASS_CARD("25", "台湾往来大陆通行证", TaWanMainlandPassDTO.class),
	/**
	 * 企业营业执照
	 */
	BUSINESS_CARD("2008","企业营业执照", BusinessCardDTO.class),
    /**
     * 未知
     */
    UNKNOWN("-1", "未知", null);

    private String type;
    private String desc;
    private Class<?> classType;

	public static CardTypeEnum cardType(String type) {
		if (Objects.nonNull(type)) {
			for (CardTypeEnum t : CardTypeEnum.values()) {
				if (Objects.nonNull(t.type) && t.type.equals(type)) {
					return t;
				}
			}
		}
		return UNKNOWN;
	}
	public static String desc(String type) {
		return cardType(type).getDesc();
	}

}
