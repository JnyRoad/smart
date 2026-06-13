package com.tce.smart.platform.wrapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.platform.api.dto.resp.SearchQuestionListRespDTO;
import com.tce.smart.platform.api.dto.resp.SearchSelectRespDTO;
import com.tce.smart.platform.core.entity.SmtQuestion;
import com.tce.smart.platform.core.entity.SmtSelect;
import com.tce.smart.platform.service.SmtSelectService;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;

public class SearchQuestionListWrapper  extends BaseWrapper<SmtQuestion, SearchQuestionListRespDTO> {

	@Autowired
	private SmtSelectService  smtSelectService;

	@Override
	protected SearchQuestionListRespDTO warp(SmtQuestion model) throws IOException {
		// TODO Auto-generated method stub

		SearchQuestionListRespDTO list = new SearchQuestionListRespDTO();
		BeanUtil.copyProperties(model,list);

		List<SmtSelect> selectList = smtSelectService.list( Wrappers.<SmtSelect>query().lambda().eq(ObjectUtil.isNotNull(model.getId()), SmtSelect::getQuestionId, model.getId()));

		List<SearchSelectRespDTO> selectListResp=new ArrayList<>();
		for (SmtSelect smtSelect : selectList) {
			SearchSelectRespDTO selectDto=new SearchSelectRespDTO();
			BeanUtil.copyProperties(smtSelect,selectDto);
			selectListResp.add(selectDto);
		}
		list.setSelectList(selectListResp);
		return list;
	}

}
