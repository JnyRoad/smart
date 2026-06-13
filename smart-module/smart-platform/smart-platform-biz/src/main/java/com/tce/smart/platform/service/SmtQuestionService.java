package com.tce.smart.platform.service;

import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.platform.core.entity.SmtQuestion;

public interface SmtQuestionService extends IService<SmtQuestion> {

	List<Map<String, Object>>  getType();




}
