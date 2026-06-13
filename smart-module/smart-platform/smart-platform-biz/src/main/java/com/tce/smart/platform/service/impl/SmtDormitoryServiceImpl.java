package com.tce.smart.platform.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.util.BeanUtils;
import com.tce.smart.common.core.util.RegexUtils;
import com.tce.smart.common.core.util.StringUtils;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.platform.api.dto.req.DormitoryFloorReqDTO;
import com.tce.smart.platform.api.dto.resp.DormitoryFloorRespDTO;
import com.tce.smart.platform.api.dto.resp.SmtDormitoryRespDTO;
import com.tce.smart.platform.core.entity.SmtDormitory;
import com.tce.smart.platform.core.entity.SmtDormitoryFloor;
import com.tce.smart.platform.core.vo.DormitoryVO;
import com.tce.smart.platform.core.mapper.SmtDormitoryMapper;
import com.tce.smart.platform.service.SmtDormitoryFloorService;
import com.tce.smart.platform.service.SmtDormitoryService;
import com.tce.smart.platform.service.dormitoryconfig.SmtDormitoryPersonService;
import com.tce.smart.tool.exception.TCEException;
import lombok.AllArgsConstructor;
import lombok.var;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 园区宿舍楼表
 *
 * @author 齐佩
 * @date 2019-04-13 18:17:25
 */
@Service
@AllArgsConstructor
public class SmtDormitoryServiceImpl extends ServiceImpl<SmtDormitoryMapper, SmtDormitory> implements SmtDormitoryService {

	private final SmtDormitoryMapper mapper;

	private final SmtDormitoryFloorService smtDormitoryFloorService;

	private final SmtDormitoryPersonService smtDormitoryPersonService;

	@Override
	public List<DormitoryFloorRespDTO> getByParkId(Integer parkId, Boolean isAccount) {
		List<DormitoryFloorRespDTO> respDTOS = new ArrayList<>();
		List<SmtDormitory> list = new ArrayList<>();
		if (Objects.nonNull(isAccount) && isAccount) {
			List<Integer> dormitoryId = smtDormitoryPersonService.getDormitoryId(SecurityUtils.getUser().getUsername(), parkId);
			if (CollUtil.isNotEmpty(dormitoryId)) {
				list = (List<SmtDormitory>) this.listByIds(dormitoryId);
			}
		}
		if (CollUtil.isEmpty(list)) {
			list = mapper.queryDormitory(parkId);
		}
		for (SmtDormitory dormitory : list) {
			DormitoryFloorRespDTO respDTO = new DormitoryFloorRespDTO();
			DormitoryFloorReqDTO reqDTO = new DormitoryFloorReqDTO();

			reqDTO.setDormitoryId(dormitory.getId());
			reqDTO.setParkId(parkId);
			Result<List<SmtDormitoryFloor>> result = smtDormitoryFloorService.queryFloor(reqDTO);
			if (result.isSuccess()) {
				List<DormitoryFloorRespDTO.Floor> floorArrayList = new ArrayList<>();
				List<SmtDormitoryFloor> floors = result.getData();
				for (SmtDormitoryFloor dormitoryFloor : floors) {
					DormitoryFloorRespDTO.Floor floor = new DormitoryFloorRespDTO.Floor();
					floor.setFloorId(dormitoryFloor.getId());
					floor.setFloorName(StringUtils.isNotEmpty(dormitoryFloor.getAliasName()) ? dormitoryFloor.getAliasName() : dormitoryFloor.getFloorName().toString());
					floorArrayList.add(floor);
				}
				respDTO.setFloors(floorArrayList);
			}
			respDTO.setParkId(dormitory.getParkId());
			respDTO.setFloorNum(dormitory.getFloorNum());
			respDTO.setId(dormitory.getId());
			respDTO.setDormitoryName(dormitory.getDormitoryName());
			respDTOS.add(respDTO);
		}
		return respDTOS;
	}

	@Override
	public List<SmtDormitoryRespDTO> getDormitoryByParkId(Integer parkId) {
		List<Integer> parkIdList = SecurityUtils.getUser().getParkIdList();
		if (!parkIdList.contains(parkId)) {
			throw new TCEException("园区不存在");
		}
		List<SmtDormitory> list = mapper.queryDormitory(parkId);
		List<SmtDormitoryRespDTO> smtDormitoryRespDTOS = new ArrayList<>();
		list.forEach(item -> {
			var smtDormitoryRespDto = new SmtDormitoryRespDTO();
			BeanUtils.copyProperties(item, smtDormitoryRespDto);
			smtDormitoryRespDTOS.add(smtDormitoryRespDto);
		});
		return smtDormitoryRespDTOS;
	}

	@Override
	public Result addDormitory(SmtDormitory smtDormitory) {
		// TODO Auto-generated method stub
		checkDormitoy(smtDormitory);
		return new Result<>(this.save(smtDormitory));
	}

	@Override
	public Result updateDormitoryById(SmtDormitory smtDormitory) {
		// TODO Auto-generated method stub
		checkDormitoy(smtDormitory);
		return new Result<>(this.updateById(smtDormitory));
	}

	public Result checkDormitoy(SmtDormitory smtDormitory) {
		if (smtDormitory == null) {
			return new Result<>(Boolean.FALSE, "宿舍楼参数不能为空");
		}
		if (!RegexUtils.matchName(smtDormitory.getDormitoryName())) {
			return new Result<>(Boolean.FALSE, "宿舍楼名称只允许汉字、字母与数字的组合,最长为30个字符");
		}
		Integer selectCount = this.count(Wrappers.<SmtDormitory>query().lambda()
				.eq(SmtDormitory::getDormitoryName, smtDormitory.getDormitoryName())
				.eq(SmtDormitory::getParkId, smtDormitory.getParkId()));
		if (selectCount > 0) {
			return new Result<>(Boolean.FALSE, "该园区的宿舍楼名称已存在");
		}
		return new Result<>();
	}

	@Override
	public Result removeDormitoryById(Integer id) {
		// TODO Auto-generated method stub、
		// 删除宿舍楼，要判断该宿舍楼是否已创建楼层，若有楼层提示删除失败，若无可以删除。
		SmtDormitoryFloor floor = new SmtDormitoryFloor();
		Integer selectCount = floor
				.selectCount(Wrappers.<SmtDormitoryFloor>query().lambda().eq(SmtDormitoryFloor::getDormitoryId, id));
		if (selectCount > 0)
			return new Result<>(Boolean.FALSE, "该宿舍楼有楼层，删除失败");
		else
			return new Result<>(mapper.deleteById(id));
	}

	@Override
	public Result getSmtDormitoryPage(Page page, SmtDormitory smtDormitory) {
		List<Integer> parkIdList = SecurityUtils.getUser().getParkIdList();
		IPage<List<DormitoryVO>> smtDormitoryPage = mapper.getSmtDormitoryPage(page, smtDormitory, parkIdList);
		return new Result<>(smtDormitoryPage);
	}

	@Override
	public SmtDormitory getByParkAndName(Integer parkId, String name) {
		return this.getOne(Wrappers.<SmtDormitory>lambdaQuery()
				.eq(SmtDormitory::getParkId, parkId)
				.eq(SmtDormitory::getDormitoryName, name)
		);
	}

}
