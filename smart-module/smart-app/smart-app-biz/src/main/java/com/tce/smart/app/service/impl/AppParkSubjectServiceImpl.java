package com.tce.smart.app.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.app.entity.AppParkSubject;
import com.tce.smart.app.mapper.AppParkSubjectMapper;
import com.tce.smart.app.service.AppParkSubjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * 园区主题
 *
 * @author mingkai.wu
 * @date 2019-04-25 09:44:25
 */
@Service
public class AppParkSubjectServiceImpl extends ServiceImpl<AppParkSubjectMapper, AppParkSubject> implements AppParkSubjectService {

	@Override
	public void deletePark(Integer id) {
		this.getBaseMapper().deleteParkById(id);
	}

	@Override
	public List<AppParkSubject> getByUnionId(Integer parkId, Integer subjectId) {
		return this.baseMapper.selectList(Wrappers.<AppParkSubject>query()
				.lambda()
				.eq(Objects.nonNull(parkId), AppParkSubject::getParkId, parkId)
				.eq(AppParkSubject::getSubjectId, subjectId));
	}

	@Override
	public List<AppParkSubject> getBySubjectIds(List<Integer> subjectIds) {
		QueryWrapper<AppParkSubject> queryWrapper = new QueryWrapper<>();
		queryWrapper.lambda().in(AppParkSubject::getSubjectId, subjectIds);
		return baseMapper.selectList(queryWrapper);
	}
}
