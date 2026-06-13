package com.tce.smart.platform.service;

import org.springframework.web.multipart.MultipartFile;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.platform.core.entity.SmtApplicationResume;

/**
 * 应聘者人际关系表
 *
 * @author 齐佩
 * @date 2019-04-22 15:25:26
 */
public interface SmtApplicationResumeService extends IService<SmtApplicationResume> {

	Result getResumeById(Integer id);

	Result updateApplicationResume(MultipartFile file, SmtApplicationResume smtApplicationResume);



}
