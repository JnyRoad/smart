package com.tce.smart.platform.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.admin.api.entity.SysDict;
import com.tce.smart.admin.api.feign.RemoteDictService;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.platform.api.dto.resp.DormitoryTypeRespDTO;
import com.tce.smart.platform.core.dto.DormitoryTypeDTO;
import com.tce.smart.platform.core.entity.*;
import com.tce.smart.platform.core.mapper.SmtDormitoryTypeMapper;
import com.tce.smart.platform.core.vo.DicContentVO;
import com.tce.smart.platform.core.vo.DormitoryTypeVO;
import com.tce.smart.platform.service.*;
import com.tce.smart.platform.service.dormitoryconfig.SmtDormitoryPersonService;
import com.tce.smart.tool.constant.DictConstants;
import lombok.AllArgsConstructor;
import lombok.var;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 园区宿舍类型
 *
 * @author 齐佩
 * @date 2019-04-13 18:16:57
 */
@Service
public class SmtDormitoryTypeServiceImpl extends ServiceImpl<SmtDormitoryTypeMapper, SmtDormitoryType>
		implements SmtDormitoryTypeService {
	@Autowired
	private  SmtDormitoryTypeMapper mapper;
	@Autowired
	private  SmtDormitoryLevelService levelService;

	@Autowired
	private SmtDormitoryRoomService roomService;
	@Autowired
	private  SmtDormitoryBedService bedService;
	@Autowired
	private  SmtDormitoryStaffService dorStaffService;
	@Autowired
	private  RemoteDictService remoteDictService;
	@Autowired
	private SmtDormitoryPersonService smtDormitoryPersonService;

	@Override
	public Result removeDormitoryTypeById(Integer id) {
		// TODO Auto-generated method stub
		// 删除宿舍类型时，需要确认是否有房间属于此类型，如果有则提示删除失败，如果无可以删除
		SmtDormitoryRoom room = new SmtDormitoryRoom();
		Integer selectCount = room
				.selectCount(Wrappers.<SmtDormitoryRoom> query().lambda().eq(SmtDormitoryRoom::getRoomType, id));

		if (selectCount > 0) {
			return new Result<>(Boolean.FALSE, "存在此宿舍类型的房间，删除失败");
		}
		levelService.remove(Wrappers.<SmtDormitoryLevel> query().lambda()
				.eq(SmtDormitoryLevel::getDormitoryTypeId, id));
		return new Result<>(this.removeById(id));
	}

	@Override
	public Result updateDormitoryTypeById(DormitoryTypeDTO dormitoryTypeDTO) {
		// TODO Auto-generated method stub
		if (dormitoryTypeDTO == null) {
			return new Result<>(Boolean.FALSE, "宿舍类型参数不能为空");
		}
		SmtDormitoryType selectCount = mapper.selectOne(Wrappers.<SmtDormitoryType> query().lambda()
				.eq(SmtDormitoryType::getTypeName, dormitoryTypeDTO.getTypeName())
				.eq(SmtDormitoryType::getParkId, dormitoryTypeDTO.getParkId()));
		if (selectCount!=null && !selectCount.getId().equals(dormitoryTypeDTO.getId())) {
			return new Result<>(Boolean.FALSE, "宿舍类型名称已存在");
		}
		SmtDormitoryType selectById = this.getById(dormitoryTypeDTO.getId());

		List<SmtDormitoryRoom> selectList = roomService.list(Wrappers.<SmtDormitoryRoom> query().lambda().eq(SmtDormitoryRoom::getRoomType, dormitoryTypeDTO.getId()));
		//修改类型的床位数。
		if(selectById.getBedTotal()<dormitoryTypeDTO.getBedTotal())
		{
			for (SmtDormitoryRoom room : selectList) {

				for (int i = 0; i <dormitoryTypeDTO.getBedTotal() - selectById.getBedTotal(); i++) {

					SmtDormitoryBed bed=new SmtDormitoryBed();
					Integer bedNum=  selectById.getBedTotal()+1+i;
					bed.setBedNumber(bedNum);
					bed.setDormitoryId(room.getDormitoryId());
					bed.setFloorId(room.getFloorId());
					bed.setParkId(room.getParkId());
					bed.setRoomId(room.getId());
					bed.insert();
				}
				room.setBedTotal(dormitoryTypeDTO.getBedTotal());
				room.updateById();
			}
		}else if(selectById.getBedTotal()>dormitoryTypeDTO.getBedTotal() )
		{
			//减床位
			boolean isHave=false;
			for (SmtDormitoryRoom room : selectList) {

				List<SmtDormitoryStaff> selectList2 = dorStaffService.list(Wrappers.<SmtDormitoryStaff> query().lambda().eq(SmtDormitoryStaff::getRoomId, room.getId()));

				for (SmtDormitoryStaff smtDormitoryStaff : selectList2) {
					if(smtDormitoryStaff.getBedNumber()>dormitoryTypeDTO.getBedTotal())
					{
						isHave=true;
						return new Result<>(Boolean.FALSE, "大床位号已办理入住，不能减少床位数量");
					}
				}
			}
			if(!isHave)
			{
				//删除所有房间的其他床位
				for (SmtDormitoryRoom room : selectList) {

					//List<SmtDormitoryBed> selectList2 = bedMapper.selectList(Wrappers.<SmtDormitoryBed> query().lambda().eq(SmtDormitoryBed::getRoomId, room.getId()));

					room.setBedTotal(dormitoryTypeDTO.getBedTotal());
					room.updateById();
					for (int i = 0; i <selectById.getBedTotal()-dormitoryTypeDTO.getBedTotal() ; i++) {

						Integer bedNum=selectById.getBedTotal()-i;
						 bedService.remove(Wrappers.<SmtDormitoryBed> query().lambda().eq(SmtDormitoryBed::getRoomId, room.getId())
								.eq(SmtDormitoryBed::getBedNumber, bedNum));
					}

				}
			}
		}

		selectById.setTypeName(dormitoryTypeDTO.getTypeName());
		selectById.setParkId(dormitoryTypeDTO.getParkId());
		selectById.setBedTotal(dormitoryTypeDTO.getBedTotal());
		mapper.updateById(selectById);
		//类型级层
		levelService.remove(Wrappers.<SmtDormitoryLevel> query().lambda()
				.eq(SmtDormitoryLevel::getDormitoryTypeId, dormitoryTypeDTO.getId()));
		List<String> jcheLists = dormitoryTypeDTO.getJches();
		if(CollUtil.isNotEmpty(jcheLists)) {
			this.buildJcheList(jcheLists, selectById.getId());
		}
		return new Result<>(Boolean.TRUE);
	}

	private Boolean buildJcheList(List<String> ids, Integer id) {
		Result<List<SysDict>> findByType = remoteDictService.findByType(DictConstants.JOB_LEVEL, SecurityConstants.FROM_IN);
		//判断集合是否为空
		Map<String, String> map = new HashMap<>();
		if(findByType != null && findByType.getData().size()>0) {
			List<SysDict> sysDicts = findByType.getData();
			for(SysDict dict : sysDicts) {
				map.put(dict.getValue(), dict.getLabel());
			}
		}
		ids.forEach(item -> {
			SmtDormitoryLevel level = new SmtDormitoryLevel();
			level.setDormitoryTypeId(id);
			level.setJcheId(item);
			level.setJcheName(map.get(item));
			levelService.save(level);
		});
		return Boolean.TRUE;
	}

	@Override
	public Result addDormitoryType(DormitoryTypeDTO dormitoryTypeDTO) {
		// TODO Auto-generated method stub
		if (dormitoryTypeDTO == null) {
			return new Result<>(Boolean.FALSE, "宿舍类型参数不能为空");
		}
		Integer selectCount = this.count(Wrappers.<SmtDormitoryType> query().lambda()
				.eq(SmtDormitoryType::getTypeName, dormitoryTypeDTO.getTypeName())
				.eq(SmtDormitoryType::getParkId,dormitoryTypeDTO.getParkId())
		);
		if (selectCount > 0) {
			return new Result<>(Boolean.FALSE, "宿舍类型名称已存在");
		}
		SmtDormitoryType smtDormitoryType=new SmtDormitoryType();
		smtDormitoryType.setTypeName(dormitoryTypeDTO.getTypeName());
		smtDormitoryType.setBedTotal(dormitoryTypeDTO.getBedTotal());
		smtDormitoryType.setParkId(dormitoryTypeDTO.getParkId());
		smtDormitoryType.insert();
		List<String> jcheLists = dormitoryTypeDTO.getJches();
		if(CollUtil.isNotEmpty(jcheLists)) {
			this.buildJcheList(jcheLists, smtDormitoryType.getId());
		}
		return new Result<>(Boolean.TRUE);
	}

	@Override
	public IPage<DormitoryTypeVO> getSmtDormitoryTypePage(Page page, SmtDormitoryType smtDormitoryType) {
		// TODO Auto-generated method stub
		List<Integer> parkIdList = smtDormitoryPersonService.getParkId(SecurityUtils.getUser().getUsername());
		if(CollUtil.isEmpty(parkIdList)) {
			parkIdList = SecurityUtils.getUser().getParkIdList();
		}
		IPage<DormitoryTypeVO> list=mapper.getSmtDormitoryTypePage(page,smtDormitoryType,parkIdList);
		list.getRecords().forEach(item -> {
			List<SmtDormitoryLevel> levels = levelService.getByType(item.getId());
			if(CollUtil.isNotEmpty(levels)) {
				item.setLevelIds(levels.stream().map(SmtDormitoryLevel::getJcheId).collect(Collectors.toList()));
				item.setLevelNames(levels.stream().map(SmtDormitoryLevel::getJcheName).collect(Collectors.toList()));
			}
		});
		return list;
	}

	@Override
	public Result getSmtDormitoryTypeAll() {
		// TODO Auto-generated method stub
		SmtDormitoryType smtDormitoryType=new SmtDormitoryType();
		List<SmtDormitoryType> selectAll = smtDormitoryType.selectAll();
		return new Result<>(selectAll);
	}

	@Override
	public List<DormitoryTypeRespDTO> getSmtDormitoryTypeByPark(Integer parkId) {
		List<Integer> parkIds = SecurityUtils.getUser().getParkIdList();
		List<SmtDormitoryType> selectByPark = this.list(Wrappers.<SmtDormitoryType>query().lambda()
				.eq(Objects.nonNull(parkId), SmtDormitoryType::getParkId, parkId)
				.in(SmtDormitoryType::getParkId, parkIds));
		List<DormitoryTypeRespDTO> dormitoryTypeRespDTOS = new ArrayList<>();
		selectByPark.forEach(item -> {
			var dormitoryTypeRespDTO = new DormitoryTypeRespDTO();
			BeanUtils.copyProperties(item,dormitoryTypeRespDTO);
			dormitoryTypeRespDTOS.add(dormitoryTypeRespDTO);
		});
		return dormitoryTypeRespDTOS;
	}

	@Override
	public List<DormitoryTypeRespDTO> getSmtDormitoryTypeByParkAndDormitory(Integer parkId, Integer dormitoryId) {
		List<Integer> parkIds = SecurityUtils.getUser().getParkIdList();
		List<SmtDormitoryRoom> dormitoryRooms = roomService.list(new LambdaQueryWrapper<SmtDormitoryRoom>()
				.eq(Objects.nonNull(parkId), SmtDormitoryRoom::getParkId, parkId)
				.in(CollUtil.isNotEmpty(parkIds), SmtDormitoryRoom::getParkId, parkIds)
				.eq(Objects.nonNull(dormitoryId), SmtDormitoryRoom::getDormitoryId, dormitoryId)
		);
		Set<Integer> roomTypeIds = dormitoryRooms.stream().map(SmtDormitoryRoom::getRoomType).collect(Collectors.toSet());
		roomTypeIds.removeIf(type -> null == type);
		if(CollectionUtil.isEmpty(roomTypeIds)){
			return new ArrayList<>();
		}
		Collection<SmtDormitoryType> dormitoryTypes = this.listByIds(roomTypeIds);
		List<DormitoryTypeRespDTO> dormitoryTypeRespDTOS = new ArrayList<>();
		dormitoryTypes.forEach(item -> {
			var dormitoryTypeRespDTO = new DormitoryTypeRespDTO();
			BeanUtils.copyProperties(item,dormitoryTypeRespDTO);
			dormitoryTypeRespDTOS.add(dormitoryTypeRespDTO);
		});
		return dormitoryTypeRespDTOS;
	}


}
