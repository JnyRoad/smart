package com.tce.smart.data.api.dto.msg.req;

import com.tce.smart.common.core.ao.BaseAO;
import lombok.Data;

/**
 * @Title: ArticlesReleaseMsgReqDTO
 * @Auther: guohongtai
 * @Date: 2020-10-19 11:00
 */
@Data
public class ArticlesReleaseMsgReqDTO extends BaseAO {
	private String phone;
	private String tempCode;
	private String url;
}
