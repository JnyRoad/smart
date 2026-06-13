package com.tce.smart.dhrview.core.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.dhrview.core.entity.YutoDhrPsndo;
import com.tce.smart.dhrview.core.mapper.YutoDhrPsndoMapper;
import com.tce.smart.dhrview.core.service.YutoDhrPsndoService;
import com.tce.smart.tool.enums.EvwEmphrYsEnum;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @description: YutoDhrPsndoService
 * @date: 2021/5/27 0027 15:48
 * @author: wuling
 * @version: 1.0
 */
@Service
public class YutoDhrPsndoServiceImpl extends ServiceImpl<YutoDhrPsndoMapper, YutoDhrPsndo> implements YutoDhrPsndoService {

	@Override
	public IPage<YutoDhrPsndo> getPage(Page page, List<Integer> buIds) {
		List<String> collect = buIds.stream().map(String::valueOf).collect(Collectors.toList());
		IPage<YutoDhrPsndo> dhrEmpList = this.baseMapper.getDhrEmpList(page, collect);
		return dhrEmpList;
	}

	@Override
	public YutoDhrPsndo getByBadge(String badge) {
		YutoDhrPsndo dhrPsndo = this.getOne(Wrappers.<YutoDhrPsndo>query().lambda().eq(StrUtil.isNotBlank(badge),YutoDhrPsndo::getCode, badge));
		return dhrPsndo;
	}

	@Override
	public YutoDhrPsndo getByUserId(String userId) {
		return this.baseMapper.getByUserId(userId);
	}

    @Override
    public List<YutoDhrPsndo> getByCompId(Integer compId) {
        return this.baseMapper.selectList(Wrappers.<YutoDhrPsndo> query().lambda().eq(YutoDhrPsndo::getPkOrg, compId));
    }

	@Override
	public List<YutoDhrPsndo> getInStaffByCompId(Integer compId) {
		return this.baseMapper.selectList(Wrappers.<YutoDhrPsndo> query().lambda().eq(YutoDhrPsndo::getPkOrg, compId).ne(YutoDhrPsndo::getJobglbdef1, EvwEmphrYsEnum.STAFF_STATUS_QUIT.getCode()));
	}
}
