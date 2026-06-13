package com.tce.smart.platform.service.impl;


import java.time.LocalDateTime;
import java.util.List;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.platform.core.entity.SmtDevice;
import com.tce.smart.platform.core.entity.SmtParking;
import com.tce.smart.platform.core.entity.SmtParkingCount;
import com.tce.smart.platform.core.mapper.SmtDeviceMapper;
import com.tce.smart.platform.core.mapper.SmtParkingMapper;
import com.tce.smart.platform.service.SmtParkingCountService;
import com.tce.smart.platform.service.SmtParkingService;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import lombok.AllArgsConstructor;

/**
 * 停车场表
 *
 * @author 王艳勇
 * @date 2019-04-13 13:48:12
 */
@Service
@AllArgsConstructor
public class SmtParkingServiceImpl extends ServiceImpl<SmtParkingMapper, SmtParking> implements SmtParkingService {
    private final SmtParkingCountService parkingCountService;
    private final SmtDeviceMapper deviceMapper;
    @Override
    public boolean saveParking(SmtParking entity) {
	entity.setCreateTime(LocalDateTime.now());
	this.save(entity);
	SmtParkingCount smtParkingCount = new SmtParkingCount();
	    smtParkingCount.setTotalCount(entity.getTotalCount());
	    smtParkingCount.setUseCount(0);
	    smtParkingCount.setFreeCount(entity.getTotalCount());
	    smtParkingCount.setParkId(entity.getParkId());
	    smtParkingCount.setParkingId(entity.getId());
	    smtParkingCount.setCreateTime(LocalDateTime.now());
        return parkingCountService.save(smtParkingCount);
    }

    @Override
    public boolean updateParking(SmtParking entity) {
        return this.updateById(entity);
    }

    @Override
    public boolean deleteParkingParking(Integer id) {
        return this.removeById(id);
    }

    @Override
    public List<SmtParking> getParking(List<Integer> parkIds) {
        return this.list(Wrappers.<SmtParking>query().lambda().in(CollUtil.isNotEmpty(parkIds),SmtParking::getParkId,parkIds).orderByDesc(SmtParking::getCreateTime));
    }

	@Override
	public IPage page(Page page, List<Integer> parkIds) {
		return this.page(page,Wrappers.<SmtParking>query().lambda().in(CollUtil.isNotEmpty(parkIds),SmtParking::getParkId,parkIds).orderByDesc(SmtParking::getCreateTime));
	}

	@Override
	public boolean removeParking(String id) {
		Integer count = deviceMapper.selectCount(Wrappers.<SmtDevice>query().lambda().eq(SmtDevice::getDeviceSubtype, id));
		if(ObjectUtil.isNull(count) || count == 0) {
			return this.removeById(id);
		}
		return false;
	}

}
