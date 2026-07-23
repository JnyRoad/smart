package com.tce.smart.platform.service.impl;


import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.admin.api.entity.SysDict;
import com.tce.smart.admin.api.feign.RemoteDictService;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.util.CollectionUtils;
import com.tce.smart.common.core.util.StringUtils;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.data.api.dto.ehrview.EvwEmphrYsDTO;
import com.tce.smart.data.api.dto.ehrview.resp.OvwYsdepRespDTO;
import com.tce.smart.data.api.feign.ehrview.RemoteEvwEmphrYsService;
import com.tce.smart.data.api.feign.ehrview.RemoteOvwYsdepService;
import com.tce.smart.data.api.feign.ehrview.RemoteOvwYsjobService;
import com.tce.smart.platform.api.dto.req.dormitorymange.DormitoryRoomReqDTO;
import com.tce.smart.platform.api.dto.req.dormitorymange.SearchDormitoryRoomDetailReqDTO;
import com.tce.smart.platform.api.dto.resp.bigdatapanel.ParkDataRespDTO;
import com.tce.smart.platform.core.dto.DormitoryTreeDTO;
import com.tce.smart.platform.core.dto.RoomTreeDTO;
import com.tce.smart.platform.core.dto.meter.DormitoryLazyQueryDTO;
import com.tce.smart.platform.core.dto.meter.DormitoryLazyTreeDTO;
import com.tce.smart.platform.core.entity.*;
import com.tce.smart.platform.core.entity.ext.DormitoryRoomExt;
import com.tce.smart.platform.core.enums.DormitoryEnum;
import com.tce.smart.platform.core.mapper.*;
import com.tce.smart.platform.core.vo.CompStatisticsVO;
import com.tce.smart.platform.core.vo.ParkStatisticsVO;
import com.tce.smart.platform.service.SmtDormitoryBedService;
import com.tce.smart.platform.service.SmtDormitoryStaffService;
import com.tce.smart.platform.service.SmtParkBuService;
import com.tce.smart.platform.service.SmtParkService;
import com.tce.smart.platform.service.dormitoryconfig.SmtDormitoryConfigService;
import com.tce.smart.platform.service.dormitoryconfig.SmtDormitoryPersonService;
import com.tce.smart.tool.constant.DictConstants;
import com.tce.smart.tool.enums.DormitoryApplyStatusEnum;
import com.tce.smart.tool.exception.TCEException;
import com.tce.smart.tool.util.RegexUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 园区表
 *
 * @author 齐佩
 * @date 2019-04-13 13:48:12
 */
@Slf4j
@Service
@AllArgsConstructor
public class SmtParkServiceImpl extends ServiceImpl<SmtParkMapper, SmtPark> implements SmtParkService {

	private final SmtParkMapper mapper;

	private final SmtDormitoryMapper dormitoryMapper;

	private final SmtDormitoryFloorMapper floorMapper;

	private final SmtDormitoryRoomMapper roomMapper;

	private final RemoteOvwYsdepService depService;

	private final RemoteOvwYsjobService jobService;

	private final RemoteEvwEmphrYsService evwEmphrYsService;

	private final RemoteDictService remoteDictService;

	private final SmtParkBuService smtParkBuService;

	private final SmtDormitoryApplyMapper smtDormitoryApplyMapper;

	private final SmtStaffMapper staffMapper;

	private final SmtDormitoryConfigService smtDormitoryConfigService;

	private final SmtDormitoryPersonService smtDormitoryPersonService;


	@Override
	public Result addPark(SmtPark smtPark) {
		// TODO Auto-generated method stub
		if (smtPark == null) {
			return new Result<>(Boolean.FALSE, "园区参数不能为空");
		}
		if (!RegexUtils.matchName(smtPark.getParkName())) {
			return new Result<>(Boolean.FALSE, "园区名称只允许汉字、字母与数字的组合,最长为30个字符");
		}
		/*if (!RegexUtils.matchLongitude(smtPark.getParkLongitude().toString())) {
			return new Result<>(Boolean.FALSE, "经度整数部分为0-180,小数部分为0到6位");
		}
		if (!RegexUtils.matchLatitude(smtPark.getParkLatitude().toString())) {
			return new Result<>(Boolean.FALSE, "纬度整数部分为0-90,小数部分为0到6位");
		}*/
	/*	if (!RegexUtils.matchPhone(smtPark.getParkPhone()) && !RegexUtils.matchTelephone(smtPark.getParkPhone()) ) {
			return new Result<>(Boolean.FALSE, "咨询电话格式不正确");
		}*/
		List<SmtPark> selectList = mapper
				.selectList(Wrappers.<SmtPark>query().lambda().eq(SmtPark::getParkName, smtPark.getParkName()));
		if (selectList.size() > 0) {
			return new Result<>(Boolean.FALSE, "园区名称已存在");
		}
		return new Result<>(this.save(smtPark), "园区添加成功");
	}

	@Override
	public Result updateParkById(SmtPark smtPark) {
		// TODO Auto-generated method stub
		if (smtPark == null) {
			return new Result<>(Boolean.FALSE, "园区参数不能为空");
		}
		if (!RegexUtils.matchName(smtPark.getParkName())) {
			return new Result<>(Boolean.FALSE, "园区名称只允许汉字、字母与数字的组合,最长为30个字符");
		}
		/*if (!RegexUtils.matchLongitude(smtPark.getParkLongitude().toString())) {
			return new Result<>(Boolean.FALSE, "经度整数部分为0-180,小数部分为0到6位");
		}
		if (!RegexUtils.matchLatitude(smtPark.getParkLatitude().toString())) {
			return new Result<>(Boolean.FALSE, "纬度整数部分为0-90,小数部分为0到6位");
		}*/
	/*	if (!RegexUtils.matchPhone(smtPark.getParkPhone()) && !RegexUtils.matchTelephone(smtPark.getParkPhone()) ) {
			return new Result<>(Boolean.FALSE, "咨询电话格式不正确");
		}*/
		List<SmtPark> selectList = mapper
				.selectList(Wrappers.<SmtPark>query().lambda().eq(SmtPark::getParkName, smtPark.getParkName()));
		if (selectList.size() > 0 && !selectList.get(0).getId().equals(smtPark.getId())) {
			return new Result<>(Boolean.FALSE, "园区名称已存在");
		}
		return new Result<>(this.updateById(smtPark), "园区修改成功");
	}

	@Override
	public Result removeParkById(Integer id) {
		// TODO Auto-generated method stub
		// 判断该园区下是否有宿舍楼，若有宿舍楼则提示删除失败，若无可以删除。
		// 删除园区要判断该园区是否有员工，招聘信息，应聘信息，等等有关联的数据。
		SmtDormitory dormitory = new SmtDormitory();
		Integer selectCount = dormitory
				.selectCount(Wrappers.<SmtDormitory>query().lambda().eq(SmtDormitory::getParkId, id));

		//Integer selectStaffCount = staffService.count(Wrappers.<SmtStaff>query().lambda().eq(SmtStaff::getParkId, id));
		if (selectCount > 0) {
			return new Result<>(Boolean.FALSE, "该园区有宿舍楼，删除失败");
		} else
			return new Result<>(mapper.deleteById(id), "园区删除成");

	}

	@Override
	public List<SmtPark> getParkList() {
		List<Integer> parkIdList = SecurityUtils.getUser().getParkIdList();
		if (parkIdList.size() == 0) {
			return mapper.selectList(Wrappers.<SmtPark>query().lambda().orderByDesc(SmtPark::getId));
		} else {
			return mapper.selectList(Wrappers.<SmtPark>query().lambda().in(SmtPark::getId, parkIdList).orderByDesc(SmtPark::getId));
		}
	}

	@Override
	public List<SmtPark> getDormitoryParks() {
		List<Integer> parkIds = smtDormitoryPersonService.getParkId(SecurityUtils.getUser().getUsername());
		List<SmtPark> parks = this.list(Wrappers.<SmtPark>query().lambda().eq(SmtPark::getId, parkIds));
		if (CollUtil.isNotEmpty(parkIds)) {
			return parks;
		}
		return this.getParkList();
	}

	public List<SmtPark> getParkList(Integer parkId) {
		List<Integer> parkIdList = SecurityUtils.getUser().getParkIdList();
		if (parkIdList.size() == 0) {
			return mapper.selectList(Wrappers.<SmtPark>query().lambda().eq(null != parkId, SmtPark::getId, parkId).orderByAsc(SmtPark::getParkName));
		} else {
			return mapper.selectList(Wrappers.<SmtPark>query().lambda().eq(null != parkId, SmtPark::getId, parkId).in(SmtPark::getId, parkIdList).orderByAsc(SmtPark::getParkName));
		}
	}

	@Override
	public List<SmtPark> getUnStrainedParks() {
		return this.list();
	}

	@Override
	public Result allList() {
		String account = SecurityUtils.getUser().getUsername();
		// TODO Auto-generated method stub
		List<SmtPark> list;
		List<Integer> parkIds = smtDormitoryPersonService.getParkId(account);
		if (CollUtil.isNotEmpty(parkIds)) {
			list = (List<SmtPark>) this.listByIds(parkIds);
		} else {
			list = getParkList();
		}
		List<SmtDormitory> queryDormitory;
		List<SmtDormitoryFloor> queryFloor;
		List<SmtDormitoryRoom> queryRoom;
		List<DormitoryTreeDTO> parkTreeList = new ArrayList<>();
		List<DormitoryTreeDTO> dormitoryTreeList;
		List<DormitoryTreeDTO> floorTreeList;
		List<DormitoryTreeDTO> roomTreeList;
		for (SmtPark smtPark : list) {
			DormitoryTreeDTO parkTree = new DormitoryTreeDTO();
			parkTree.setId(smtPark.getId());
			parkTree.setLabel(smtPark.getParkName());
			List<Integer> dormitoryIds = smtDormitoryPersonService.getDormitoryId(account, smtPark.getId());
			if (CollUtil.isNotEmpty(dormitoryIds)) {
				queryDormitory = dormitoryMapper.selectList(Wrappers.<SmtDormitory>query().lambda().in(SmtDormitory::getId, dormitoryIds));
			} else {
				queryDormitory = dormitoryMapper.queryDormitory(smtPark.getId());
			}
			dormitoryTreeList = new ArrayList<>();
			for (SmtDormitory smtDormitory : queryDormitory) {
				DormitoryTreeDTO dormitoryTree = new DormitoryTreeDTO();
				dormitoryTree.setId(smtDormitory.getId());
				dormitoryTree.setLabel(smtDormitory.getDormitoryName());
				queryFloor = floorMapper.queryFloor(smtDormitory.getId());
				floorTreeList = new ArrayList<>();
				for (SmtDormitoryFloor smtDormitoryFloor : queryFloor) {
					DormitoryTreeDTO floorTree = new DormitoryTreeDTO();
					floorTree.setId(smtDormitoryFloor.getId());
					floorTree.setLabel(StringUtils.isNotEmpty(smtDormitoryFloor.getAliasName()) ? smtDormitoryFloor.getAliasName() : smtDormitoryFloor.getFloorName().toString());
					queryRoom = roomMapper.queryRoom(smtDormitoryFloor.getId());
					roomTreeList = new ArrayList<>();
					for (SmtDormitoryRoom smtDormitoryRoom : queryRoom) {
						DormitoryTreeDTO roomTree = new DormitoryTreeDTO();
						roomTree.setId(smtDormitoryRoom.getId());
						roomTree.setLabel(StringUtils.isNotEmpty(smtDormitoryRoom.getAliasName()) ? smtDormitoryRoom.getAliasName() : smtDormitoryRoom.getRoomName().toString());
						roomTreeList.add(roomTree);
					}
					floorTree.setChildren(roomTreeList);
					floorTreeList.add(floorTree);
				}

				dormitoryTree.setChildren(floorTreeList);
				dormitoryTreeList.add(dormitoryTree);
			}
			parkTree.setChildren(dormitoryTreeList);
			parkTreeList.add(parkTree);

		}

		return new Result<>(parkTreeList);
	}

	@Override
	public Result lazyPark(DormitoryLazyQueryDTO queryDTO) {
		if (DormitoryEnum.PARK.getCode().equals(queryDTO.getType())) {
			return new Result<>(getParkLazyTree());
		} else if (DormitoryEnum.DORMITORY.getCode().equals(queryDTO.getType())) {
			return new Result<>(getDormitoryLazyTree(queryDTO.getParentId()));
		} else if (DormitoryEnum.FLOOR.getCode().equals(queryDTO.getType())) {
			return new Result<>(getFloorLazyTree(queryDTO.getParentId()));
		} else {
			return new Result<>(getRoomLazyTree(queryDTO.getParentId()));
		}
	}

	/**
	 * 园区懒加载
	 *
	 * @return
	 */
	private List<DormitoryLazyTreeDTO> getParkLazyTree() {
		List<DormitoryLazyTreeDTO> lazyTreeList = new ArrayList<>();
		String account = SecurityUtils.getUser().getUsername();
		List<SmtPark> list;
		List<Integer> parkIds = smtDormitoryPersonService.getParkId(account);
		if (CollUtil.isNotEmpty(parkIds)) {
			list = (List<SmtPark>) this.listByIds(parkIds);
		} else {
			list = getParkList();
		}
		for (SmtPark smtPark : list) {
			DormitoryLazyTreeDTO treeDTO = new DormitoryLazyTreeDTO();
			treeDTO.setId(smtPark.getId());
			treeDTO.setLabel(smtPark.getParkName());
			List<Integer> dormitoryIds = smtDormitoryPersonService.getDormitoryId(account, smtPark.getId());
			if (CollUtil.isNotEmpty(dormitoryIds)) {
				treeDTO.setHadChild(dormitoryMapper.selectCount(Wrappers.<SmtDormitory>query().lambda()
						.in(SmtDormitory::getId, dormitoryIds)) > 0);
			} else {
				treeDTO.setHadChild(dormitoryMapper.queryDormitory(smtPark.getId()).size() > 0);
			}
			lazyTreeList.add(treeDTO);
		}
		return lazyTreeList;
	}

	/**
	 * 楼栋懒加载
	 *
	 * @param parentId
	 * @return
	 */
	private List<DormitoryLazyTreeDTO> getDormitoryLazyTree(Integer parentId) {
		String account = SecurityUtils.getUser().getUsername();
		List<DormitoryLazyTreeDTO> treeList = new ArrayList<>();
		SmtPark smtPark = this.getById(parentId);
		List<Integer> dormitoryIds = smtDormitoryPersonService.getDormitoryId(account, smtPark.getId());
		List<SmtDormitory> queryDormitory;
		if (CollUtil.isNotEmpty(dormitoryIds)) {
			queryDormitory = dormitoryMapper.selectList(Wrappers.<SmtDormitory>query().lambda().in(SmtDormitory::getId, dormitoryIds));
		} else {
			queryDormitory = dormitoryMapper.queryDormitory(smtPark.getId());
		}
		for (SmtDormitory smtDormitory : queryDormitory) {
			DormitoryLazyTreeDTO dormitoryTree = new DormitoryLazyTreeDTO();
			dormitoryTree.setId(smtDormitory.getId());
			dormitoryTree.setLabel(smtDormitory.getDormitoryName());
			dormitoryTree.setHadChild(floorMapper.queryFloor(smtDormitory.getId()).size() > 0);
			treeList.add(dormitoryTree);
		}
		return treeList;
	}

	/**
	 * 楼层懒加载
	 *
	 * @param parentId
	 * @return
	 */
	private List<DormitoryLazyTreeDTO> getFloorLazyTree(Integer parentId) {
		List<DormitoryLazyTreeDTO> treeList = new ArrayList<>();
		List<SmtDormitoryFloor> queryFloor = floorMapper.queryFloor(parentId);
		for (SmtDormitoryFloor smtDormitoryFloor : queryFloor) {
			DormitoryLazyTreeDTO floorTree = new DormitoryLazyTreeDTO();
			floorTree.setId(smtDormitoryFloor.getId());
			floorTree.setLabel(StringUtils.isNotEmpty(smtDormitoryFloor.getAliasName()) ? smtDormitoryFloor.getAliasName()
					: smtDormitoryFloor.getFloorName().toString());
			floorTree.setHadChild(roomMapper.queryRoom(smtDormitoryFloor.getId()).size() > 0);
			treeList.add(floorTree);
		}
		return treeList;
	}

	/**
	 * 房间懒加载
	 *
	 * @param parentId
	 * @return
	 */
	private List<DormitoryLazyTreeDTO> getRoomLazyTree(Integer parentId) {
		List<DormitoryLazyTreeDTO> treeList = new ArrayList<>();
		List<SmtDormitoryRoom> queryRoom = roomMapper.queryRoom(parentId);
		for (SmtDormitoryRoom smtDormitoryRoom : queryRoom) {
			DormitoryLazyTreeDTO roomTree = new DormitoryLazyTreeDTO();
			roomTree.setId(smtDormitoryRoom.getId());
			roomTree.setLabel(StringUtils.isNotEmpty(smtDormitoryRoom.getAliasName()) ? smtDormitoryRoom.getAliasName()
					: smtDormitoryRoom.getRoomName().toString());
			roomTree.setHadChild(Boolean.FALSE);
			treeList.add(roomTree);
		}
		return treeList;
	}

	@Override
	public List<DormitoryTreeDTO> getDormitoryTreeNonRoom() {
		List<SmtPark> list = getParkList();

		List<SmtDormitory> queryDormitory;// = new ArrayList<>();
		List<SmtDormitoryFloor> queryFloor;// = new ArrayList<>();
		List<DormitoryTreeDTO> parkTreeList = new ArrayList<>();
		List<DormitoryTreeDTO> dormitoryTreeList = null;
		List<DormitoryTreeDTO> floorTreeList = null;
		for (SmtPark smtPark : list) {
			DormitoryTreeDTO parkTree = new DormitoryTreeDTO();
			parkTree.setId(smtPark.getId());
			parkTree.setLabel(smtPark.getParkName());
			queryDormitory = dormitoryMapper.queryDormitory(smtPark.getId());
			dormitoryTreeList = new ArrayList<>();
			for (SmtDormitory smtDormitory : queryDormitory) {
				DormitoryTreeDTO dormitoryTree = new DormitoryTreeDTO();
				dormitoryTree.setId(smtDormitory.getId());
				dormitoryTree.setLabel(smtDormitory.getDormitoryName());
				queryFloor = floorMapper.queryFloor(smtDormitory.getId());
				floorTreeList = new ArrayList<>();
				for (SmtDormitoryFloor smtDormitoryFloor : queryFloor) {
					DormitoryTreeDTO floorTree = new DormitoryTreeDTO();
					floorTree.setId(smtDormitoryFloor.getId());
					floorTree.setLabel(StringUtils.isNotEmpty(smtDormitoryFloor.getAliasName()) ? smtDormitoryFloor.getAliasName() : smtDormitoryFloor.getFloorName().toString());
					floorTreeList.add(floorTree);
				}
				dormitoryTree.setChildren(floorTreeList);
				dormitoryTreeList.add(dormitoryTree);
			}
			parkTree.setChildren(dormitoryTreeList);
			parkTreeList.add(parkTree);

		}

		return parkTreeList;
	}

	@Override
	public List<DormitoryTreeDTO> dormRoomTree(SearchDormitoryRoomDetailReqDTO roomDetailReqDTO) {
		List<DormitoryTreeDTO> parkTreeList = new ArrayList<>();

		//查询园区
		List<SmtPark> parkList = getParkList(roomDetailReqDTO.getParkId());
		List<Integer> parkIds = parkList.stream().map(p -> p.getId()).collect(Collectors.toList());

		//查询楼栋
		List<SmtDormitory> queryDormitory = dormitoryMapper.selectList(new LambdaQueryWrapper<SmtDormitory>()
				.eq(null != roomDetailReqDTO.getDormitoryId(), SmtDormitory::getId, roomDetailReqDTO.getDormitoryId())
				.in(CollectionUtils.isNotEmpty(parkIds), SmtDormitory::getParkId, parkIds)
		);
		List<Integer> dormId = queryDormitory.stream().map(d -> d.getId()).collect(Collectors.toList());

		//查询楼层
		List<SmtDormitoryFloor> queryFloor = floorMapper.selectList(new LambdaQueryWrapper<SmtDormitoryFloor>()
				.eq(null != roomDetailReqDTO.getFloorId(), SmtDormitoryFloor::getId, roomDetailReqDTO.getFloorId())
				.in(CollectionUtils.isNotEmpty(dormId), SmtDormitoryFloor::getDormitoryId, dormId)
		);
		List<Integer> floorId = queryFloor.stream().map(f -> f.getId()).collect(Collectors.toList());

		//查询房间
		List<SmtDormitoryRoom> queryRoom = roomMapper.selectList(new LambdaQueryWrapper<SmtDormitoryRoom>()
				.eq(null != roomDetailReqDTO.getRoomType(), SmtDormitoryRoom::getRoomType, roomDetailReqDTO.getRoomType())
				.eq(null != roomDetailReqDTO.getSex(), SmtDormitoryRoom::getRoomSex, roomDetailReqDTO.getSex())
				.in(CollectionUtils.isNotEmpty(floorId), SmtDormitoryRoom::getFloorId, floorId)
		);

		//遍历园区
		for (SmtPark smtPark : parkList) {
			DormitoryTreeDTO parkTree = new DormitoryTreeDTO();
			parkTree.setId(smtPark.getId());
			parkTree.setLabel(smtPark.getParkName());
			List<DormitoryTreeDTO> dormitoryTreeList = new ArrayList<>();
			//遍历楼栋
			for (SmtDormitory smtDormitory : queryDormitory) {
				DormitoryTreeDTO dormitoryTree = new DormitoryTreeDTO();
				dormitoryTree.setId(smtDormitory.getId());
				dormitoryTree.setLabel(smtDormitory.getDormitoryName());
				List<DormitoryTreeDTO> floorTreeList = new ArrayList<>();
				//遍历楼层
				for (SmtDormitoryFloor smtDormitoryFloor : queryFloor) {
					DormitoryTreeDTO floorTree = new DormitoryTreeDTO();
					floorTree.setId(smtDormitoryFloor.getId());
					floorTree.setLabel(smtDormitoryFloor.getFloorName().toString());
					List<DormitoryTreeDTO> roomTreeList = new ArrayList<>();
					//遍历房间
					for (SmtDormitoryRoom smtDormitoryRoom : queryRoom) {
						DormitoryTreeDTO roomTree = new DormitoryTreeDTO();
						roomTree.setId(smtDormitoryRoom.getId());
						roomTree.setLabel(smtDormitoryRoom.getRoomName().toString());
						roomTreeList.add(roomTree);
					}
					floorTree.setChildren(roomTreeList);
					floorTreeList.add(floorTree);
				}
				dormitoryTree.setChildren(floorTreeList);
				dormitoryTreeList.add(dormitoryTree);
			}
			parkTree.setChildren(dormitoryTreeList);
			parkTreeList.add(parkTree);
		}
		return parkTreeList;
	}

	@Override
	public List<DormitoryTreeDTO> roomTree(Integer parkId) {
		List<DormitoryTreeDTO> parkTreeList = new ArrayList<>();

		//查询楼栋
		List<SmtDormitory> queryDormitory = dormitoryMapper.selectList(new LambdaQueryWrapper<SmtDormitory>()
				.eq(SmtDormitory::getParkId, parkId)
		);
		List<Integer> dormId = queryDormitory.stream().map(d -> d.getId()).collect(Collectors.toList());
		//查询楼层
		List<SmtDormitoryFloor> queryFloor = floorMapper.selectList(new LambdaQueryWrapper<SmtDormitoryFloor>()
				.in(CollectionUtils.isNotEmpty(dormId), SmtDormitoryFloor::getDormitoryId, dormId)
		);
		List<Integer> floorId = queryFloor.stream().map(f -> f.getId()).collect(Collectors.toList());
		//查询房间
		List<SmtDormitoryRoom> queryRoom = roomMapper.selectList(new LambdaQueryWrapper<SmtDormitoryRoom>()
				.in(CollectionUtils.isNotEmpty(floorId), SmtDormitoryRoom::getFloorId, floorId)
		);

		//遍历园区
		for (SmtDormitory smtDormitory : queryDormitory) {
			DormitoryTreeDTO dormitoryTree = new DormitoryTreeDTO();
			dormitoryTree.setId(smtDormitory.getId());
			dormitoryTree.setLabel(smtDormitory.getDormitoryName());
			List<DormitoryTreeDTO> floorTreeList = new ArrayList<>();
			List<SmtDormitoryFloor> floors = queryFloor.stream().filter(floor -> floor.getDormitoryId().equals(smtDormitory.getId())).collect(Collectors.toList());
			//遍历楼层
			for (SmtDormitoryFloor smtDormitoryFloor : floors) {
				DormitoryTreeDTO floorTree = new DormitoryTreeDTO();
				floorTree.setId(smtDormitoryFloor.getId());
				floorTree.setLabel(smtDormitoryFloor.getFloorName().toString());
				List<DormitoryTreeDTO> roomTreeList = new ArrayList<>();
				//遍历房间
				List<SmtDormitoryRoom> rooms = queryRoom.stream().filter(floor -> floor.getFloorId().equals(smtDormitoryFloor.getId())).collect(Collectors.toList());
				for (SmtDormitoryRoom smtDormitoryRoom : rooms) {
					DormitoryTreeDTO roomTree = new DormitoryTreeDTO();
					roomTree.setId(smtDormitoryRoom.getId());
					roomTree.setLabel(smtDormitoryRoom.getRoomName().toString());
					roomTreeList.add(roomTree);
				}
				floorTree.setChildren(roomTreeList);
				floorTreeList.add(floorTree);
			}
			dormitoryTree.setChildren(floorTreeList);
			parkTreeList.add(dormitoryTree);
		}
		return parkTreeList;
	}

	@Override
	public List<DormitoryTreeDTO> dormRoomTreeByRoomId(Integer roomId) {
		SmtDormitoryRoom dormitoryRoom = roomMapper.selectById(roomId);
		if (null == dormitoryRoom) {
			log.error("id为{}的房间不存在", roomId);
			throw new TCEException("房间不存在");
		}

		//查询房间
		List<SmtDormitoryRoom> queryRoom = roomMapper.selectList(new LambdaQueryWrapper<SmtDormitoryRoom>()
				//	.eq(null != dormitoryRoom.getRoomType(),SmtDormitoryRoom::getRoomType,dormitoryRoom.getRoomType())
				.eq(null != dormitoryRoom.getRoomSex(), SmtDormitoryRoom::getRoomSex, dormitoryRoom.getRoomSex())
				.eq(SmtDormitoryRoom::getParkId, dormitoryRoom.getParkId())
		);

		return getDormRoomTree(queryRoom, dormitoryRoom.getParkId());
	}

	public List<DormitoryTreeDTO> getDormRoomTree(List<? extends SmtDormitoryRoom> queryRoom, Integer parkId) {
		if (CollectionUtils.isEmpty(queryRoom)) {
			return null;
		}
		List<DormitoryTreeDTO> parkTreeList = new ArrayList<>();
		Map<Integer, List<SmtDormitoryRoom>> roomCollect = queryRoom.stream().collect(Collectors.groupingBy(SmtDormitoryRoom::getFloorId));
		List<Integer> floorIds = queryRoom.stream().map(SmtDormitoryRoom::getFloorId).collect(Collectors.toList());
		List<Integer> dorIds = queryRoom.stream().map(SmtDormitoryRoom::getDormitoryId).collect(Collectors.toList());

		//查询楼栋
		List<SmtDormitory> queryDormitory = dormitoryMapper.selectList(new LambdaQueryWrapper<SmtDormitory>()
				.in(CollectionUtils.isNotEmpty(dorIds), SmtDormitory::getId, dorIds)
		);

		//查询楼层
		List<SmtDormitoryFloor> queryFloor = floorMapper.selectList(new LambdaQueryWrapper<SmtDormitoryFloor>()
				.in(CollectionUtils.isNotEmpty(floorIds), SmtDormitoryFloor::getId, floorIds)
		);
		Map<Integer, List<SmtDormitoryFloor>> floorCollect = queryFloor.stream().collect(Collectors.groupingBy(SmtDormitoryFloor::getDormitoryId));

		//查询园区
		List<SmtPark> parkList = getParkList(parkId);


		for (SmtPark smtPark : parkList) {
			DormitoryTreeDTO parkTree = new DormitoryTreeDTO();
			parkTree.setId(smtPark.getId());
			parkTree.setLabel(smtPark.getParkName());
			List<DormitoryTreeDTO> dormitoryTreeList = new ArrayList<>();
			//遍历楼栋
			for (SmtDormitory smtDormitory : queryDormitory) {
				DormitoryTreeDTO dormitoryTree = new DormitoryTreeDTO();
				dormitoryTree.setId(smtDormitory.getId());
				dormitoryTree.setLabel(smtDormitory.getDormitoryName());
				List<DormitoryTreeDTO> floorTreeList = new ArrayList<>();
				//遍历楼层
				for (SmtDormitoryFloor smtDormitoryFloor : floorCollect.get(smtDormitory.getId())) {
					DormitoryTreeDTO floorTree = new DormitoryTreeDTO();
					floorTree.setId(smtDormitoryFloor.getId());
					floorTree.setLabel(StringUtils.isNotEmpty(smtDormitoryFloor.getAliasName()) ? smtDormitoryFloor.getAliasName() : smtDormitoryFloor.getFloorName().toString());
					List<DormitoryTreeDTO> roomTreeList = new ArrayList<>();
					//遍历房间
					List<SmtDormitoryRoom> smtDormitoryRooms = roomCollect.get(smtDormitoryFloor.getId());
					smtDormitoryRooms = smtDormitoryRooms.stream().sorted(Comparator.comparing(SmtDormitoryRoom::getRoomName)).collect(Collectors.toList());
					for (SmtDormitoryRoom smtDormitoryRoom : smtDormitoryRooms) {
						DormitoryTreeDTO roomTree = new DormitoryTreeDTO();
						roomTree.setId(smtDormitoryRoom.getId());
						roomTree.setLabel(StringUtils.isNotEmpty(smtDormitoryRoom.getAliasName()) ? smtDormitoryRoom.getAliasName() : smtDormitoryRoom.getRoomName().toString());
						roomTreeList.add(roomTree);
					}
					floorTree.setChildren(roomTreeList);
					floorTreeList.add(floorTree);
				}
				dormitoryTree.setChildren(floorTreeList);
				dormitoryTreeList.add(dormitoryTree);
			}
			parkTree.setChildren(dormitoryTreeList);
			parkTreeList.add(parkTree);
		}
		return parkTreeList;
	}

	@Override
	public List<DormitoryTreeDTO> dormRoomTreeByApplyId(Long applyId) {
		//查询申请记录
		SmtDormitoryApply dormitoryApply = smtDormitoryApplyMapper.selectById(applyId);
		if (null == dormitoryApply) {
			throw new TCEException("申请记录不存在");
		} else if (!DormitoryApplyStatusEnum.APPLYING.getCode().equals(dormitoryApply.getStatus())) {
			throw new TCEException("申请状态异常");
		}
		//查询员工信息
		SmtStaff staff = staffMapper.selectOne(new LambdaQueryWrapper<SmtStaff>().eq(SmtStaff::getBadge, dormitoryApply.getStaffBadge()));
		//查询房间
		List<SmtDormitoryRoom> queryRoom = roomMapper.selectList(new LambdaQueryWrapper<SmtDormitoryRoom>()
				.eq(SmtDormitoryRoom::getRoomSex, staff.getSex())
				.eq(SmtDormitoryRoom::getParkId, dormitoryApply.getParkId())
		);

		return getDormRoomTree(queryRoom, dormitoryApply.getParkId());
	}

	@Override
	public List<DormitoryTreeDTO> getRoomTreeByCondition(DormitoryRoomReqDTO dormitoryRoomReqDTO) {
		SmtDormitoryRoom searchRoom = new SmtDormitoryRoom();
		BeanUtils.copyProperties(dormitoryRoomReqDTO, searchRoom);
		List<DormitoryRoomExt> roomListByCondition = roomMapper.getRoomListByCondition(searchRoom, dormitoryRoomReqDTO.getFreeBedNum(), null);
		return getDormRoomTree(roomListByCondition, dormitoryRoomReqDTO.getParkId());
	}


	@Override
	public SmtPark locationPark(SmtPark smtPark) {
		SmtPark rsSmtPark = null;

		Page<SmtPark> page = new Page<SmtPark>();
		IPage<SmtPark> smtParkList = baseMapper.getDistanceList(page, smtPark);
		if (CollectionUtils.isNotEmpty(smtParkList.getRecords())) {
			rsSmtPark = smtParkList.getRecords().get(0);
		}
		return rsSmtPark;
	}

	@Override
	public Result statistics(SmtDormitoryBedService bedService, SmtDormitoryStaffService dormitoryStaffService, Integer parkId) {
		// TODO Auto-generated method stub

		//List<Integer> parkIdList = SecurityUtils.getUser().getParkIdList();

		ParkStatisticsVO parkStatisticsVO = new ParkStatisticsVO();

		SmtPark byId = this.getById(parkId);
		parkStatisticsVO.setParkArea(byId.getArea());
		parkStatisticsVO.setDiningRoomTotal(byId.getDiningRoomNum());
		parkStatisticsVO.setWorkshopToal(byId.getWorkShopNum());

		//查询园区的住宿楼数量
		Integer dormitoryToTal = dormitoryMapper.selectCount(Wrappers.<SmtDormitory>query().lambda().eq(SmtDormitory::getParkId, parkId));
		parkStatisticsVO.setDormitoryToTal(dormitoryToTal);

		//查询房间数量
		Integer roomCount = roomMapper.selectCount(Wrappers.<SmtDormitoryRoom>query().lambda().eq(SmtDormitoryRoom::getParkId, parkId));
		parkStatisticsVO.setRoomTotal(roomCount);
		//查询总床位
		Integer bedCount = bedService.count(Wrappers.<SmtDormitoryBed>query().lambda().eq(SmtDormitoryBed::getParkId, parkId));
		parkStatisticsVO.setBedTotal(bedCount);
		//查询入住员工数
		Integer dormitoryStaffCount = dormitoryStaffService.count(Wrappers.<SmtDormitoryStaff>query().lambda().eq(SmtDormitoryStaff::getParkId, parkId));
		parkStatisticsVO.setBedStaffTotal(dormitoryStaffCount);
		//查询该园区的bug

		List<SmtParkBu> parkBuList = smtParkBuService.list(Wrappers.<SmtParkBu>query().lambda().eq(SmtParkBu::getParkId, parkId));
		parkStatisticsVO.setCompTotal(parkBuList.size());
		Integer deptTotal = 0;
		Integer jobTotal = 0;
		//统计5个BU的部门总数

		for (SmtParkBu smtParkBu : parkBuList) {
			if (smtParkBu.getCompId().length() <= 10) {
				Integer compId = Integer.parseInt(smtParkBu.getCompId());
				//根据compid获取部门
				Result<List<OvwYsdepRespDTO>> depResult = depService.getByCompId(compId, SecurityConstants.FROM_IN, SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED);
				if (depResult.isSuccess()) {
					if (depResult.getData() != null) {
						List<OvwYsdepRespDTO> depData = depResult.getData();
						deptTotal += depData.size();

					}
				}
				Result<Integer> jobResult = jobService.getByCompId(compId, SecurityConstants.FROM_IN, SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED);
				if (jobResult.isSuccess()) {
					if (jobResult.getData() != null) {
						Integer jobSize = jobResult.getData();
						jobTotal += jobSize;
					}
				}
			}
		}

		parkStatisticsVO.setDeptToal(deptTotal);
		parkStatisticsVO.setJobTotal(jobTotal);
		//获取员工数据
		ParkStatisticsVO vO = compStatistics(parkStatisticsVO);

		return new Result<>(vO);
	}

	/**
	 * 统计BU的员工数据
	 *
	 * @return
	 */
	private ParkStatisticsVO compStatistics(ParkStatisticsVO parkStatisticsVO) {

		Integer total = 0;
		Result<List<SysDict>> findByType = remoteDictService.findByType(DictConstants.COMP_ABBR, SecurityConstants.FROM_IN);
		List<SysDict> data = findByType.getData();
		List<CompStatisticsVO> compStatisticsList = new ArrayList<>();
		for (SysDict sysDict : data) {
			Integer compId = Integer.parseInt(sysDict.getValue());
			CompStatisticsVO compStatisticsVO = new CompStatisticsVO();
			compStatisticsVO.setCompName(sysDict.getDescription());
			Result<List<EvwEmphrYsDTO>> emphrResult = evwEmphrYsService.getInStaffByCompId(compId, SecurityConstants.FROM_IN, SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED);
			if (emphrResult.isSuccess()) {
				if (emphrResult.getData() != null) {
					List<EvwEmphrYsDTO> emphrData = emphrResult.getData();
					total += emphrData.size();
					compStatisticsVO.setVlaue(emphrData.size());
				} else {
					compStatisticsVO.setVlaue(0);
				}
			}
			compStatisticsList.add(compStatisticsVO);
		}
		parkStatisticsVO.setCompStatistics(compStatisticsList);
		parkStatisticsVO.setStaffTotal(total);
		return parkStatisticsVO;
	}

	private List<SmtPark> getParkListToLock(Integer parkId) {
		if (Objects.isNull(parkId)) {
			return mapper.selectList(Wrappers.<SmtPark>query().lambda().orderByDesc(SmtPark::getId));
		} else {
			return mapper.selectList(Wrappers.<SmtPark>query().lambda().eq(SmtPark::getId, parkId));
		}
	}

	private Result dormitoryList(SmtDormitoryStaffService dormitoryStaffService, List<SmtPark> list, Boolean flag) {
		// TODO Auto-generated method stub
		List<Integer> parkIds = list.stream().map(SmtPark::getId).collect(Collectors.toList());

		List<SmtDormitory> queryDormitory;// = new ArrayList<>();
		List<SmtDormitoryFloor> queryFloor;// = new ArrayList<>();
		List<SmtDormitoryRoom> queryRoom;// = new ArrayList<>();
		List<RoomTreeDTO> parkTreeList = new ArrayList<>();
		List<RoomTreeDTO> dormitoryTreeList;
		List<RoomTreeDTO> floorTreeList;
		List<RoomTreeDTO> roomTreeList;

		List<SmtDormitory> smtDormitories = dormitoryMapper.selectList(new LambdaQueryWrapper<SmtDormitory>().in(SmtDormitory::getParkId, parkIds));
		Map<Integer, List<SmtDormitory>> dormitoryMap = smtDormitories.stream().collect(Collectors.groupingBy(SmtDormitory::getParkId));
		List<Integer> dormitoryIds = smtDormitories.stream().map(SmtDormitory::getId).collect(Collectors.toList());


		List<SmtDormitoryFloor> smtDormitoryFloors = floorMapper.selectList(new LambdaQueryWrapper<SmtDormitoryFloor>().in(SmtDormitoryFloor::getDormitoryId, dormitoryIds));
		Map<Integer, List<SmtDormitoryFloor>> floorMap = smtDormitoryFloors.stream().collect(Collectors.groupingBy(SmtDormitoryFloor::getDormitoryId));
		List<Integer> floorIds = smtDormitoryFloors.stream().map(SmtDormitoryFloor::getId).collect(Collectors.toList());

		List<SmtDormitoryRoom> smtDormitoryRooms = roomMapper.selectList(new LambdaQueryWrapper<SmtDormitoryRoom>().in(SmtDormitoryRoom::getFloorId, floorIds));
		Map<Integer, List<SmtDormitoryRoom>> roomMap = smtDormitoryRooms.stream().collect(Collectors.groupingBy(SmtDormitoryRoom::getFloorId));

		for (SmtPark smtPark : list) {
			RoomTreeDTO parkTree = new RoomTreeDTO();
			parkTree.setId(smtPark.getId());
			parkTree.setLabel(smtPark.getParkName());
			queryDormitory = dormitoryMap.get(smtPark.getId());
			if (CollectionUtils.isEmpty(queryDormitory)) {
				continue;
			}
			// 对楼栋按照名称排序
			queryDormitory = queryDormitory.stream().sorted(Comparator.comparing(SmtDormitory::getDormitoryName))
					.collect(Collectors.toList());
			dormitoryTreeList = new ArrayList<>();

			for (SmtDormitory smtDormitory : queryDormitory) {
				RoomTreeDTO dormitoryTree = new RoomTreeDTO();
				dormitoryTree.setId(smtDormitory.getId());
				dormitoryTree.setLabel(smtDormitory.getDormitoryName());
				queryFloor = floorMap.get(smtDormitory.getId());
				if (CollectionUtils.isEmpty(queryFloor)) {
					continue;
				}
				// 对楼层排序
				queryFloor = queryFloor.stream().sorted(Comparator.comparingInt(SmtDormitoryFloor::getFloorName))
						.collect(Collectors.toList());
				floorTreeList = new ArrayList<>();
				for (SmtDormitoryFloor smtDormitoryFloor : queryFloor) {
					RoomTreeDTO floorTree = new RoomTreeDTO();
					floorTree.setId(smtDormitoryFloor.getId());
					floorTree.setLabel(StringUtils.isNotEmpty(smtDormitoryFloor.getAliasName()) ? smtDormitoryFloor.getAliasName() : smtDormitoryFloor.getFloorName().toString());
					queryRoom = roomMap.get(smtDormitoryFloor.getId());
					if (CollectionUtils.isEmpty(queryRoom)) {
						continue;
					}
					roomTreeList = new ArrayList<>();

					List<Integer> currRoomIds = queryRoom.stream().map(SmtDormitoryRoom::getId).collect(Collectors.toList());
					List<DormitoryRoomExt> roomBedUse = dormitoryStaffService.getRoomBedUse(currRoomIds);
					Map<Integer, List<DormitoryRoomExt>> roomBedUseMap = roomBedUse.stream().collect(Collectors.groupingBy(DormitoryRoomExt::getId));
					//房间排序
					queryRoom = queryRoom.stream().sorted(Comparator.comparingInt(SmtDormitoryRoom::getRoomName))
							.collect(Collectors.toList());
					for (SmtDormitoryRoom smtDormitoryRoom : queryRoom) {
						RoomTreeDTO roomTree = new RoomTreeDTO();
						roomTree.setId(smtDormitoryRoom.getId());

						//roomTree.setTotal(smtDormitoryRoom.getBedTotal());
						Integer total = smtDormitoryRoom.getBedTotal();
						Integer usedCount = 0;
						if (roomBedUseMap.containsKey(smtDormitoryRoom.getId())) {
							usedCount = roomBedUseMap.get(smtDormitoryRoom.getId()).get(0).getUseBedNum();
						}
						if (flag) {
							Integer free = total - usedCount;
							String ss = "(" + total + "/" + usedCount + "/" + free + ")";
							roomTree.setLabel((StringUtils.isNotEmpty(smtDormitoryRoom.getAliasName()) ? smtDormitoryRoom.getAliasName() : smtDormitoryRoom.getRoomName().toString()) + ss);
						} else {
							roomTree.setLabel(StringUtils.isNotEmpty(smtDormitoryRoom.getAliasName()) ? smtDormitoryRoom.getAliasName() : smtDormitoryRoom.getRoomName().toString());
						}
						roomTreeList.add(roomTree);
					}
					floorTree.setChildren(roomTreeList);
					floorTreeList.add(floorTree);
				}

				dormitoryTree.setChildren(floorTreeList);
				dormitoryTreeList.add(dormitoryTree);
			}
			parkTree.setChildren(dormitoryTreeList);
			parkTreeList.add(parkTree);

		}

		return new Result<>(parkTreeList);
	}

	@Override
	public Result dormitoryAllList(SmtDormitoryStaffService dormitoryStaffService) {
		List<SmtPark> list = this.getParkList();
		return this.dormitoryList(dormitoryStaffService, list, Boolean.TRUE);
	}


	@Override
	public Result dormitoryAllListToLock(SmtDormitoryStaffService dormitoryStaffService, Integer parkId) {
		List<SmtPark> smtParks = this.getParkListToLock(parkId);
		return this.dormitoryList(dormitoryStaffService, smtParks, Boolean.FALSE);
	}

	@Override
	public ParkDataRespDTO getParkData() {
		return null;
	}


}
