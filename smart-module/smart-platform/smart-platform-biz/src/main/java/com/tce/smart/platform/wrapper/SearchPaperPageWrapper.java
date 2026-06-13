package com.tce.smart.platform.wrapper;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tce.smart.admin.api.entity.SysDict;
import com.tce.smart.admin.api.feign.RemoteDictService;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.platform.api.dto.resp.SearchOutDormitoryRespDTO;
import com.tce.smart.platform.core.entity.SmtPaper;
import com.tce.smart.platform.core.entity.SmtPaperBu;
import com.tce.smart.platform.core.entity.SmtPark;
import com.tce.smart.platform.core.entity.SmtParkBu;
import com.tce.smart.platform.core.entity.SmtStaff;
import com.tce.smart.platform.core.vo.SearchOutDormitoryVO;
import com.tce.smart.platform.core.vo.SearchPaperPageVO;
import com.tce.smart.platform.service.SmtPaperBuService;
import com.tce.smart.platform.service.SmtParkBuService;
import com.tce.smart.platform.service.SmtParkService;
import com.tce.smart.tool.constant.DictConstants;

import cn.hutool.core.bean.BeanUtil;
import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class SearchPaperPageWrapper extends BaseWrapper<SmtPaper, SearchPaperPageVO> {

	@Autowired
	private SmtParkService smtParkService;

	private final RemoteDictService remoteDictService;
	@Autowired
	private SmtPaperBuService  smtPaperBuService;

	@Override
	protected SearchPaperPageVO warp(SmtPaper model) throws IOException {
		// TODO Auto-generated method stub
		SearchPaperPageVO vo=new SearchPaperPageVO();
		BeanUtil.copyProperties(model, vo);
		SmtPark byId = smtParkService.getById(model.getParkId());
		vo.setParkName(byId.getParkName());
		List<SmtPaperBu> list = smtPaperBuService.list(Wrappers.<SmtPaperBu>query().lambda().eq(SmtPaperBu::getPaperId, model.getId()));
		String compNames="";
		Result<List<SysDict>> findByType = remoteDictService.findByType(DictConstants.COMP_ABBR,SecurityConstants.FROM_IN);
		List<SysDict> data = findByType.getData();
		for (SmtPaperBu smtPaperBu : list) {
			 for (SysDict sysDict : data) {
				 if(sysDict.getValue().equals(smtPaperBu.getCompId().toString()))
				 {
					 compNames+=sysDict.getDescription()+",";
					 break;
				 }
			}
		}
		 if(!compNames.equals("")) {
			 compNames=compNames.substring(0,compNames.length()-1);
		 }
		 vo.setCompNames(compNames);
		return vo;
	}

}
