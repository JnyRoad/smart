package com.tce.smart.platform.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.data.api.dto.ehrview.resp.OvwYscompRespDTO;
import com.tce.smart.data.api.feign.ehrview.RemoteOvwYscompService;
import com.tce.smart.platform.core.entity.SmtPark;
import com.tce.smart.platform.core.entity.SmtParkBu;
import com.tce.smart.platform.core.mapper.SmtParkBuMapper;
import com.tce.smart.platform.service.SmtOrganizeRelationService;
import com.tce.smart.platform.service.SmtParkBuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 园区BU关系表
 *x`
 * @author mckaywu
 * @date 2019-11-20 10:35:16
 */
@Service
public class SmtParkBuServiceImpl extends ServiceImpl<SmtParkBuMapper, SmtParkBu> implements SmtParkBuService {

	@Autowired
	private SmtParkBuMapper mapper;

	@Autowired
	private RemoteOvwYscompService remoteOvwYscompService;

	@Override
	public List<SmtPark> getParkListByBu(Long compId) {
		List<SmtPark> parkList=mapper.getParkListByBu(compId);
		return parkList;
	}

	@Override
	public List<SmtPark> getUserParkListByBu(Integer compId, List<Integer> parkIds) {
		List<SmtPark> parkList=mapper.getUserParkByBu(compId, parkIds);
		return parkList;
	}

	@Override
	public List<SmtParkBu> listByParkId(Integer parkId) {
		return this.list(Wrappers.<SmtParkBu>query().lambda().eq(SmtParkBu::getParkId, parkId));
	}

	@Override
	public List<OvwYscompRespDTO> getAllByParkId(Integer parkId) {
		List<SmtParkBu> parkBus = this.list(Wrappers.<SmtParkBu>query().lambda().eq(SmtParkBu::getParkId, parkId));
		List<OvwYscompRespDTO>  listOvwYscompVO = new ArrayList<>();
		// 查询裕同视图中的全部bu
		parkBus.forEach(bu->{
			Result<OvwYscompRespDTO> result = remoteOvwYscompService.getByCompId(bu.getCompId(), SecurityConstants.FROM_IN);
			if(result.isSuccess() && Objects.nonNull(result.getData())) {
				listOvwYscompVO.add(result.getData());
			}
		});
		return listOvwYscompVO;
	}

	@Override
	public Boolean removeByParkId(Integer parkId) {
		return this.remove(Wrappers.<SmtParkBu>query().lambda().eq(SmtParkBu::getParkId, parkId));
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Boolean saveParkBu(Integer parkId, List<String> workCompList) {
		this.removeByParkId(parkId);
		SmtParkBu smtParkBu;
		for (String element : workCompList) {
			smtParkBu = new SmtParkBu();
			smtParkBu.setParkId(parkId);
			smtParkBu.setCompId(element);
			smtParkBu.setCreateTime(LocalDateTime.now());
			smtParkBu.insert();
		}
		//smtOrganizeRelationService.saveSyncBu(workCompList, parkId);
		return Boolean.TRUE;
	}
}
