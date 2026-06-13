package com.tce.smart.platform.api.dto.resp;


import lombok.Data;

/**
 * 微信绑定表
 *
 * @author sunfujian
 * @since 2021-10-11 17:20:23
 */
@Data
public class WechatBandingRespDTO {
	private static final long serialVersionUID = 1L;
	private Long id;
	/**
	 * OPEN_ID
	 */
    private String openId;
    /**
   * UNION_ID
   */
    private String unionId;
    /**
   * 工号
   */
    private String badge;

}
