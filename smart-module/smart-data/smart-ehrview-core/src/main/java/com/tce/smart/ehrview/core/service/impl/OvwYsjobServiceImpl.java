package com.tce.smart.ehrview.core.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.ehrview.core.entity.OvwYsjob;
import com.tce.smart.ehrview.core.mapper.OvwYsjobMapper;
import com.tce.smart.ehrview.core.service.IOvwYsjobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
public class OvwYsjobServiceImpl extends ServiceImpl<OvwYsjobMapper, OvwYsjob> implements IOvwYsjobService {

	@Autowired
	private  OvwYsjobMapper mapper;

    @Override
    public List<OvwYsjob> getByDeptId(Integer deptId) {
        return this.baseMapper.selectList(Wrappers.<OvwYsjob> query().lambda().eq(OvwYsjob::getDepID, deptId).orderByAsc(OvwYsjob::getDepID));
    }

    @Override
    public OvwYsjob getByJobId(String jobId) {
        return this.baseMapper.selectOne(Wrappers.<OvwYsjob> query().lambda().eq(OvwYsjob::getJobid, jobId));
    }

	@Override
	public Integer getByCompId(Integer compId) {
		// TODO Auto-generated method stub
		return mapper.getByCompId(compId);
	}

	@Override
	public List<OvwYsjob> getListByCompId(Integer compId) {
		// TODO Auto-generated method stub
		return mapper.getListByCompId(compId);
	}

	@Override
	public List<OvwYsjob> getJchenList() {
		return mapper.getJchenList();
	}

}
