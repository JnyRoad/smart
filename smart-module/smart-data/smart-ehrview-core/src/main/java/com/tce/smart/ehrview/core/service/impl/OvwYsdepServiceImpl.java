package com.tce.smart.ehrview.core.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.ehrview.core.entity.OvwYsdep;
import com.tce.smart.ehrview.core.mapper.OvwYsdepMapper;
import com.tce.smart.ehrview.core.service.IOvwYsdepService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author WangJinbo123
 * @since 2019-05-03
 */
@Service
public class OvwYsdepServiceImpl extends ServiceImpl<OvwYsdepMapper, OvwYsdep> implements IOvwYsdepService {

    @Override
    public List<OvwYsdep> getByCompId(Integer compId) {
        return this.baseMapper.selectList(Wrappers.<OvwYsdep> query().lambda().eq(OvwYsdep::getCompID, compId));
    }

    @Override
    public OvwYsdep getByDepId(Integer depId) {
        return this.baseMapper.selectOne(Wrappers.<OvwYsdep> query().lambda().eq(OvwYsdep::getDepid, depId));
    }

	@Override
	public List<OvwYsdep> getParentDep(Integer depId) {
		// TODO Auto-generated method stub

		OvwYsdep selectOne = this.baseMapper.selectOne(Wrappers.<OvwYsdep> query().lambda().eq(OvwYsdep::getDepid, depId));
		List<OvwYsdep> list=new ArrayList<OvwYsdep>();
		list.add(selectOne);
		Integer grade=Integer.parseInt(ObjectUtil.isNotNull(selectOne)?selectOne.getDepGrade():"0");

		for (int i = grade; i > 0; i--) {

			if( selectOne.getAdminID()!=null) {
			 selectOne = this.baseMapper.selectOne(Wrappers.<OvwYsdep> query().lambda().eq(OvwYsdep::getDepid, selectOne.getAdminID()));
			 list.add(selectOne);
			}
		}
		return list;
	}
}
