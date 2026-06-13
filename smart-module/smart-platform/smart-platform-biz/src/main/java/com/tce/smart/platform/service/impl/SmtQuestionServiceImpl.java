package com.tce.smart.platform.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.platform.core.entity.SmtQuestion;
import com.tce.smart.platform.core.mapper.SmtQuestionMapper;
import com.tce.smart.platform.service.SmtQuestionService;
import com.tce.smart.platform.service.SmtSelectService;
import com.tce.smart.tool.enums.QuestionTypeEnum;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class SmtQuestionServiceImpl  extends ServiceImpl<SmtQuestionMapper, SmtQuestion> implements SmtQuestionService {

	@Autowired
	private SmtSelectService  smtSelectService;

	@Override
	public List<Map<String, Object>>  getType() {
		// TODO Auto-generated method stub
		 List<Map<String, Object>> typeList = QuestionTypeEnum.getTypeList();
		 return typeList;
	}


}
