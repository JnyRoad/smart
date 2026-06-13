package com.tce.smart.platform.api.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @author: luohongwen.
 * @Date:Created in 2019/11/7 .
 * @Description: 门禁卡片应答DTO
 */
@Data
public class BridgeListenerDTO implements Serializable {
	private static final long serialVersionUID = 1L;
    private String content;
}
