package com.tce.smart.data.service.dhr.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.common.core.util.BeanUtils;
import com.tce.smart.data.api.dto.dhrview.resp.YutoDhrPsndoDTO;
import com.tce.smart.data.service.dhr.DhrPsndoService;
import com.tce.smart.dhrview.core.entity.YutoDhrPsndo;
import com.tce.smart.dhrview.core.mapper.YutoDhrPsndoMapper;
import com.tce.smart.dhrview.core.service.YutoDhrPsndoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @description: YutoDhrPsndoService
 * @date: 2021/5/27 0027 15:48
 * @author: wuling
 * @version: 1.0
 */
@Service
public class DhrPsndoServiceImpl extends ServiceImpl<YutoDhrPsndoMapper, YutoDhrPsndo> implements DhrPsndoService {

	@Autowired
	private YutoDhrPsndoService yutoDhrPsndoService;

	@Override
	public Page<YutoDhrPsndoDTO> getPage(Page page, List<Integer> buIds) {
		IPage<YutoDhrPsndo> dhrEmpList = yutoDhrPsndoService.getPage(page, buIds);
		Page<YutoDhrPsndoDTO> resPage = new Page<>(page.getCurrent(),page.getSize(),page.getTotal());
		if(CollectionUtil.isNotEmpty(dhrEmpList.getRecords())){
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			List<YutoDhrPsndoDTO> dhrPsndoDTOS = new ArrayList<>();
			dhrEmpList.getRecords().forEach(item -> {
				YutoDhrPsndoDTO dhrPsndoDTO = new YutoDhrPsndoDTO();
				BeanUtils.copyProperties(item,dhrPsndoDTO);
				try {
					dhrPsndoDTO.setGlbdef7(dateFormat.parse(item.getGlbdef7()));
				}
				catch (Exception e){

				}
				dhrPsndoDTOS.add(dhrPsndoDTO);
			});
			resPage.setRecords(dhrPsndoDTOS);
		}

		return resPage;
	}

	@Override
	public String getProperties(String badge) {
		YutoDhrPsndo dhrEmp = yutoDhrPsndoService.getByBadge(badge);
		if(Objects.nonNull(dhrEmp)) {
			return dhrEmp.getJobglbdef22();
		}
		return null;
	}

	@Override
	public String getBadgeByUserId(String userId) {
		YutoDhrPsndo dhrPsndo = yutoDhrPsndoService.getByUserId(userId);
		return dhrPsndo == null ? null : dhrPsndo.getCode();
	}
}
