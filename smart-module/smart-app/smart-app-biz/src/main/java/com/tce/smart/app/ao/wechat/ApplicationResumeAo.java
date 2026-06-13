package com.tce.smart.app.ao.wechat;

import org.springframework.web.multipart.MultipartFile;

import lombok.Data;

/**
 * 投递简历文件信息
 * @author tce
 *
 */
@Data
public class ApplicationResumeAo {

	private String applicationId;


	private MultipartFile attachFile;

}
