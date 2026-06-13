package com.tce.smart.app.ao.wechat;

import org.springframework.web.multipart.MultipartFile;

import lombok.Data;

/**
 * 简历添加
 * @author qipei
 *
 */
@Data
public class AttachmentVAo {


	private  MultipartFile attachFile;

	private String applicationId;
}
