package com.tce.smart.admin.api.vo;

import lombok.Data;

/**
 *
 * @date 2018/8/27
 * 前端日志vo
 */
@Data
public class PreLogVO {
	private String url;
	private String time;
	private String user;
	private String type;
	private String message;
	private String stack;
	private String info;
}
