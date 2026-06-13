package com.tce.smart.platform.api.dto.req;


import lombok.Data;

import java.util.List;

/**
 * 微信绑定表
 *
 * @author fushiping
 * @date 2021-10-09 17:20:23
 */
@Data
public class WechatBandingReqDTO{
private static final long serialVersionUID = 1L;

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

    private Integer parkId;

    private String name;
}
