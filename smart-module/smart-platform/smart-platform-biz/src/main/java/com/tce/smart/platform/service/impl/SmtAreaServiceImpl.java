package com.tce.smart.platform.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.util.RegexUtils;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.platform.core.entity.SmtAlarmDevice;
import com.tce.smart.platform.core.entity.SmtArea;
import com.tce.smart.platform.core.entity.SmtDeviceArea;
import com.tce.smart.platform.core.entity.SmtPark;
import com.tce.smart.platform.core.mapper.SmtAreaMapper;
import com.tce.smart.platform.core.vo.AreaTreeChildren;
import com.tce.smart.platform.core.vo.AreaTreeParent;
import com.tce.smart.platform.core.vo.AreaTreeRoot;
import com.tce.smart.platform.core.vo.SearchAreaVO;
import com.tce.smart.platform.service.SmtAreaService;
import com.tce.smart.platform.service.SmtParkService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 地点表
 *
 * @author 齐佩
 * @date 2019-04-13 13:48:18
 */
@Service
@AllArgsConstructor
@Slf4j
public class SmtAreaServiceImpl extends ServiceImpl<SmtAreaMapper, SmtArea> implements SmtAreaService {
	private final SmtAreaMapper mapper;
	@Autowired
	private SmtParkService parkService;

	@Override
	public Result addArea(SmtArea smtArea) {
		if (smtArea == null) {
			return new Result<>(Boolean.FALSE, "地点参数不能为空");
		}
		if (!RegexUtils.matchName(smtArea.getAreaName())) {
			return new Result<>(Boolean.FALSE, "地点名称只允许汉字、字母与数字的组合，最长为30个字符");
		}
		Integer selectCount = this.count(Wrappers.<SmtArea>query().lambda().eq(SmtArea::getAreaName, smtArea.getAreaName()).eq(SmtArea::getParkId, smtArea.getParkId()));
		if (selectCount > 0) {
			return new Result<>(Boolean.FALSE, "该园区的地点名称已存在");
		}
		return new Result<>(this.save(smtArea));
	}

	@Override
	public Result updateAreaById(SmtArea smtArea) {
		if (smtArea == null) {
			return new Result<>(Boolean.FALSE, "地点参数不能为空");
		}
		if (!RegexUtils.matchName(smtArea.getAreaName())) {
			return new Result<>(Boolean.FALSE, "地点名称只允许汉字、字母与数字的组合，最长为30个字符");
		}
		List<SmtArea> selectCount = this.list(Wrappers.<SmtArea>query().lambda()
				.eq(SmtArea::getAreaName, smtArea.getAreaName())
				.eq(SmtArea::getParkId, smtArea.getParkId()));
		if (selectCount.size() > 0 && !selectCount.get(0).getId().equals(smtArea.getId())) {
			return new Result<>(Boolean.FALSE, "该园区的地点名称已存在");
		}
		return new Result<>(this.updateById(smtArea));
	}

	@Override
	public Result removeAreaById(Integer id) {
		//删除二级子节点
		List<SmtArea> area = this.list(Wrappers.<SmtArea>query().lambda().eq(SmtArea::getPid, id));
		List<Integer> ids = new ArrayList<>();
		ids.add(id);
		if(CollUtil.isNotEmpty(area)) {
			List<Integer> idList = area.stream().map(SmtArea::getId).collect(Collectors.toList());
			ids.addAll(idList);
		}
		return this.checkDeviceRelation(ids);
	}

	private Result checkDeviceRelation(List<Integer> id) {
		// 删除地点的时候，需要判断是否有数据管理该地点，如果有提示删除失败
		SmtDeviceArea deviceArea = new SmtDeviceArea();
		Integer deviceAreaCount = deviceArea
				.selectCount(Wrappers.<SmtDeviceArea>query().lambda().in(SmtDeviceArea::getAreaId, id));

		if (deviceAreaCount > 0) {
			return new Result<>(Boolean.FALSE, "该地点已绑定设备，删除失败");
		}
		SmtAlarmDevice alarmDevice = new SmtAlarmDevice();
		Integer alarmDeviceCount = alarmDevice
				.selectCount(Wrappers.<SmtAlarmDevice>query().lambda().in(SmtAlarmDevice::getAreaId, id));
		if (alarmDeviceCount > 0) {
			return new Result<>(Boolean.FALSE, "该地点已绑定警报设备，删除失败");
		}
		return new Result<>(this.removeByIds(id));
	}

	@Override
	public IPage<SearchAreaVO> getSmtAreaPage(Page page, SmtArea smtArea) {
		List<Integer> parkIdList = SecurityUtils.getUser().getParkIdList();
		IPage<SearchAreaVO> list = mapper.getSmtAreaPage(page, smtArea, parkIdList);
		return list;
	}

	@Override
	public List<AreaTreeRoot> getSmtAreaAll() {
		List<SmtPark> selectAllPark;
		List<Integer> parkIdList = SecurityUtils.getUser().getParkIdList();
		if (parkIdList.size() > 0)
			selectAllPark = parkService.list(Wrappers.<SmtPark>query().lambda().in(SmtPark::getId, parkIdList));
		else
			selectAllPark = parkService.list();
		List<AreaTreeRoot> rooList = new ArrayList<>();
		SmtArea area = new SmtArea();
		for (SmtPark smtPark : selectAllPark) {
			AreaTreeRoot root = new AreaTreeRoot();
			root.setId(smtPark.getId());
			root.setLabel(smtPark.getParkName());
			root.setPid(null);
			root.setPName(null);
			area.setParkId(smtPark.getId());
			area.setPid(0);
			List<SmtArea> list = mapper.getSmtAreaAll(area);
			List<AreaTreeParent> parentList = new ArrayList<>();
			for (SmtArea smtArea : list) {
				AreaTreeParent parent = new AreaTreeParent();
				parent.setId(smtArea.getId());
				parent.setLabel(smtArea.getAreaName());
				parent.setPid(0);
				parent.setPName(smtPark.getParkName());
				parent.setAreaLatitude(smtArea.getAreaLatitude());
				parent.setAreaLongitude(smtArea.getAreaLongitude());
				parent.setRemark(smtArea.getRemark());
				parent.setParkName(smtPark.getParkName());
				area.setPid(smtArea.getId());
				List<SmtArea> list2 = mapper.getSmtAreaAll(area);
				List<AreaTreeChildren> childList = new ArrayList<>();
				for (SmtArea smtArea2 : list2) {
					AreaTreeChildren child = new AreaTreeChildren();
					child.setParkName(smtPark.getParkName());
					child.setId(smtArea2.getId());
					child.setLabel(smtArea2.getAreaName());
					child.setPid(smtArea.getId());
					child.setPName(smtArea.getAreaName());
					child.setAreaLatitude(smtArea2.getAreaLatitude());
					child.setAreaLongitude(smtArea2.getAreaLongitude());
					child.setRemark(smtArea2.getRemark());
					childList.add(child);
				}
				parent.setChildren(childList);
				parentList.add(parent);
			}
			root.setChildren(parentList);
			rooList.add(root);
		}
		return rooList;
	}

	@Override
	public SmtArea getByName(Integer parkId, String areName) {
		return this.getOne(Wrappers.<SmtArea>lambdaQuery()
				.eq(SmtArea::getParkId, parkId)
				.eq(SmtArea::getAreaName, areName));
	}
}
