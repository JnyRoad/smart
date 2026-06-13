package com.tce.smart.ehrview.core.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.tool.enums.EvwEmphrYsEnum;

import lombok.extern.slf4j.Slf4j;

import com.tce.smart.common.core.util.StringUtils;
import com.tce.smart.ehrview.core.entity.EvwEmphrYs;
import com.tce.smart.ehrview.core.mapper.EvwEmphrYsMapper;
import com.tce.smart.ehrview.core.service.IEvwEmphrYsService;

import java.util.List;

import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author WangJinbo123
 * @since 2019-05-03
 */
@Slf4j
@Service
public class EvwEmphrYsServiceImpl extends ServiceImpl<EvwEmphrYsMapper, EvwEmphrYs> implements IEvwEmphrYsService {

    @Override
    public EvwEmphrYs getByBadge(String badge) {
        return this.baseMapper.selectOne(Wrappers.<EvwEmphrYs> query().lambda().eq(EvwEmphrYs::getBadge, badge));
    }

	@Override
	public List<EvwEmphrYs> getByCompId(Integer compId) {
		// TODO Auto-generated method stub
		return this.baseMapper.selectList(Wrappers.<EvwEmphrYs> query().lambda().eq(EvwEmphrYs::getCompID, compId));
	}

	@Override
	public IPage<EvwEmphrYs> getPage(Page page, List<Integer> compId) {
		return this.baseMapper.getPage(page, compId);
	}

	@Override
	public List<EvwEmphrYs> getInStaffByCompId(Integer compId) {
		// TODO Auto-generated method stub
		return this.baseMapper.selectList(Wrappers.<EvwEmphrYs> query().lambda().eq(EvwEmphrYs::getCompID, compId).ne(EvwEmphrYs::getStatus, EvwEmphrYsEnum.STAFF_STATUS_QUIT.getCode()));
	}

	@Override
	public IPage<EvwEmphrYs> getBlack(Page page, String certNo,String name) {
		// TODO Auto-generated method stub
		IPage<EvwEmphrYs> pageResult=this.baseMapper.selectPage(page,Wrappers.<EvwEmphrYs> query().lambda().like(StringUtils.isNotBlank(certNo), EvwEmphrYs::getCertno, certNo).like(StringUtils.isNotBlank(name), EvwEmphrYs::getName, name).eq(EvwEmphrYs::getIsBlackList, 1));
		 log.info(pageResult.toString());
		 return pageResult;
	}
}
