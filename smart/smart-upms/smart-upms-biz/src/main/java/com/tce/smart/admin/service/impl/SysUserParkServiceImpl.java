package com.tce.smart.admin.service.impl;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.admin.api.entity.SysUserPark;
import com.tce.smart.admin.mapper.SysUserParkMapper;
import com.tce.smart.admin.service.SysUserParkService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/***
 * description: 用户园区表 服务实现类 <br>
 * date: 2019/11/20 17:29 <br>
 * author: mckaywu <br>
 * version: 1.0 <br>
 */
@Service
public class SysUserParkServiceImpl extends ServiceImpl<SysUserParkMapper, SysUserPark> implements SysUserParkService {

	@Override
	public List<SysUserPark> getListByUserId(Integer userId) {
		return this.list(Wrappers.<SysUserPark>update().lambda().eq(SysUserPark::getUserId,userId));
	}

	@Override
	public boolean deleteByUserId(Integer userId) {
		return this.remove(Wrappers.<SysUserPark>update().lambda().eq(SysUserPark::getUserId,userId));
		//return baseMapper.deleteByUserId(userId);
	}

	@Override
	public boolean saveUserParkBatch(Integer userId,List<Integer> parkIdList) {
		if (Objects.nonNull(userId) && CollectionUtils.isNotEmpty(parkIdList)) {
			List<SysUserPark> userParkList = parkIdList.stream().map(parkId -> {
				SysUserPark userPark = new SysUserPark();
				userPark.setUserId(userId);
				userPark.setParkId(parkId);
				userPark.setCreateTime(LocalDateTime.now());
				return userPark;
			}).collect(Collectors.toList());

			return this.saveBatch(userParkList);
		}

		return Boolean.FALSE;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean updateUserPark(Integer userId, List<Integer> parkIdList) {
		if (CollectionUtils.isNotEmpty(parkIdList)) {
			if (this.deleteByUserId(userId)) {
				return this.saveUserParkBatch(userId, parkIdList);
			}
		}
		return Boolean.FALSE;
	}

	@Override
	public List<SysUserPark> getUserParkList(Integer userId) {
		return this.list(Wrappers.<SysUserPark>query()
				.lambda()
				.eq(SysUserPark::getUserId, userId));
	}

}
