package com.tce.smart.schedule.dto;

import cn.hutool.json.JSONObject;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Li.JiaJun
 * @since 2022/6/28 16:25
 */
@Data
public class DownDetailDTO implements Serializable {

	private String personId;

	private String downResultCode;

	private JSONObject personDownloadDetail;
}
