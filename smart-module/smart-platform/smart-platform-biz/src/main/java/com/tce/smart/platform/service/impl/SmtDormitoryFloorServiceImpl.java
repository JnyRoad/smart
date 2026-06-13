package com.tce.smart.platform.service.impl;

import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.platform.api.dto.req.DormitoryFloorReqDTO;
import com.tce.smart.platform.core.dto.DormitoryFloorDTO;
import com.tce.smart.platform.core.entity.SmtDormitoryBed;
import com.tce.smart.platform.core.entity.SmtDormitoryFloor;
import com.tce.smart.platform.core.entity.SmtDormitoryRoom;
import com.tce.smart.platform.core.entity.SmtDormitoryStaff;
import com.tce.smart.platform.core.vo.FloorVO;
import com.tce.smart.platform.core.mapper.SmtDormitoryFloorMapper;
import com.tce.smart.platform.core.mapper.SmtDormitoryRoomMapper;
import com.tce.smart.platform.service.SmtDormitoryBedService;
import com.tce.smart.platform.service.SmtDormitoryFloorService;
import com.tce.smart.platform.service.SmtDormitoryStaffService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/*import com.tce.smart.common.core.constant.enums.DormitoryTypeEnum;*/

/**
 * 园区宿舍楼的楼层
 *
 * @author 齐佩
 * @date 2019-04-13 18:17:15
 */
@Service
@AllArgsConstructor
public class SmtDormitoryFloorServiceImpl extends ServiceImpl<SmtDormitoryFloorMapper, SmtDormitoryFloor>
		implements SmtDormitoryFloorService {

	private final SmtDormitoryFloorMapper mapper;

	private final SmtDormitoryRoomMapper roomMapper;


	private final SmtDormitoryBedService bedService;

	private final SmtDormitoryStaffService dorStaffService;


	@Override
	public Result updateDormitoryFloorById(SmtDormitoryFloor smtDormitoryFloor) {
		// TODO Auto-generated method stub
		// 宿舍楼id
		Integer floorId = smtDormitoryFloor.getId();
		List<SmtDormitoryStaff> selectList2 = dorStaffService.list(Wrappers.<SmtDormitoryStaff> query().lambda().eq(SmtDormitoryStaff::getFloorId, floorId));
		if(selectList2.size()>0)
		{
			return new Result<>(Boolean.FALSE,"该楼层有人员入住，修改失败");
		}

		List<SmtDormitoryBed> selectList = bedService.list(Wrappers.<SmtDormitoryBed> query().lambda().eq(SmtDormitoryBed::getFloorId, floorId));
		if(selectList.size()>0)
		{
			return new Result<>(Boolean.FALSE,"该楼层有床位，修改失败");
		}


		SmtDormitoryFloor selectById = this.getById(floorId);
		if (selectById.getRoomNum() != null) {
			if (selectById.getRoomNum() < smtDormitoryFloor.getRoomNum()) {
				// 如果大于原始层数，则追加。
				return  roomInsert(smtDormitoryFloor);
			} else if (selectById.getRoomNum() > smtDormitoryFloor.getRoomNum()) {
				return  roomDelete(smtDormitoryFloor);
			}
		}
		return new Result<>(this.updateById(smtDormitoryFloor));
	}


	public Result roomDelete(SmtDormitoryFloor smtDormitoryFloor)
	{
		Integer floorId = smtDormitoryFloor.getId();
		SmtDormitoryFloor selectById = this.getById(floorId);
		SmtDormitoryRoom room = new SmtDormitoryRoom();
		room.setFloorId(floorId);

		for (int i = 0; i < selectById.getRoomNum() - smtDormitoryFloor.getRoomNum(); i++) {
			Integer roomNum = selectById.getRoomNum() - i;
			//根据房间数，获取房间对象
			SmtDormitoryRoom selectOne = roomMapper.selectOne(Wrappers.<SmtDormitoryRoom> query().lambda().eq(SmtDormitoryRoom::getFloorId, floorId)
					.eq(SmtDormitoryRoom::getParkId, smtDormitoryFloor.getParkId())
					.eq(SmtDormitoryRoom::getDormitoryId, smtDormitoryFloor.getDormitoryId())
					.eq(SmtDormitoryRoom::getRoomNum, roomNum));

			boolean delete = selectOne.deleteById();
			if(delete)
			{
				bedService.remove(Wrappers.<SmtDormitoryBed> query().lambda().eq(SmtDormitoryBed::getRoomId, selectOne.getId()));
			}
			System.out.println(delete);
		}
		return new Result<>(this.updateById(smtDormitoryFloor));
	}

	/**
	 * 修改楼层的房间数大于原始房间数
	 * @param smtDormitoryFloor
	 * @return
	 */
	public Result roomInsert(SmtDormitoryFloor smtDormitoryFloor)
	{

		Integer floorId = smtDormitoryFloor.getId();
		SmtDormitoryFloor selectById = this.getById(floorId);

		for (int i = 0; i < smtDormitoryFloor.getRoomNum() - selectById.getRoomNum(); i++) {

			SmtDormitoryRoom room = new SmtDormitoryRoom();
			room.setFloorId(floorId);
			room.setParkId(selectById.getParkId());
			room.setDormitoryId(selectById.getDormitoryId());
			Integer roomNum = selectById.getRoomNum() + i + 1;
			room.setRoomNum(roomNum);
			if (roomNum < 10) {
				String roomSt = selectById.getFloorName().toString() + "0" + roomNum;
				room.setRoomName(Integer.parseInt(roomSt));
			} else {
				String roomSt = selectById.getFloorName().toString() + roomNum;
				room.setRoomName(Integer.parseInt(roomSt));
			}
			room.setRoomSex(0);
			room.setIsDormitoryRoom(0);

		/*	SmtDormitoryLevel one2 = levelService.getOne(Wrappers.<SmtDormitoryLevel> query().lambda().eq(SmtDormitoryLevel::getJcheName, "员工层"));

			//默认是员工
			SmtDormitoryType one = typeService.getById(one2.getDormitoryTypeId());*/
			//room.setRoomType(one.getId());
			room.setBedTotal(0);
			room.insert();
			//添加床位数BedTotal
			/*for (int j = 1; j < one.getBedTotal()+1; j++) {

				SmtDormitoryBed bed=new SmtDormitoryBed();
				bed.setBedNumber(j);
				bed.setDormitoryId(selectById.getDormitoryId());
				bed.setFloorId(floorId);
				bed.setParkId(selectById.getParkId());
				bed.setRoomId(room.getId());
				bed.insert();
			}*/
		}
		return new Result<>(this.updateById(smtDormitoryFloor));
	}


	@Override
	public Result<List<SmtDormitoryFloor>> queryFloor(DormitoryFloorReqDTO smtDormitoryFloor) {
		//园区ID和楼栋ID必须设置
		Assert.notNull(smtDormitoryFloor.getParkId(),"园区ID不能为NULL");
		Assert.notNull(smtDormitoryFloor.getDormitoryId(),"楼栋ID不能为NULL");
		QueryWrapper<SmtDormitoryFloor> queryWrapper = new QueryWrapper<>();
		queryWrapper.lambda()
				.eq(SmtDormitoryFloor::getParkId,smtDormitoryFloor.getParkId())
				.eq(SmtDormitoryFloor::getDormitoryId,smtDormitoryFloor.getDormitoryId())
				.orderByAsc(SmtDormitoryFloor::getId);
		List<SmtDormitoryFloor> floorList=mapper.selectList(queryWrapper);
		return new Result<>(floorList);
	}


	@Override
	public Result addFloor(DormitoryFloorDTO dormitoryFloorDTO) {
		// TODO Auto-generated method stub

		Integer startNum=dormitoryFloorDTO.getStartNum();
		if (dormitoryFloorDTO.getFloorNum() != null) {

			SmtDormitoryFloor floor = new SmtDormitoryFloor();
			Boolean isHa=false;
			for (int i =startNum; i <=dormitoryFloorDTO.getFloorNum()+startNum-1;i++) {

				floor.setDormitoryId(dormitoryFloorDTO.getDormitoryId());
				floor.setParkId(dormitoryFloorDTO.getParkId());
				Integer floorNum=i;
				if(i==0){
					isHa=true;
				}
				if(isHa) {
					floorNum = i+1 ;
				}
				floor.setFloorName(floorNum);
				floor.setRoomNum(0);
				floor.insert();
			}

		}

//		Integer dormitoryId = dormitoryFloorDTO.getId();
//		SmtDormitory selectById = dormitoryMapper.selectById(dormitoryId);
//		if (dormitoryFloorDTO.getFloorNum() != null) {
//			// 如果大于原始层数，则添加。如果小于原始层数，判断之后的层数里是否有房间，如果有房间，提示。如果没有房间删除。
//			if (selectById.getFloorNum() < dormitoryFloorDTO.getFloorNum()) {
//				// 如果大于原始层数，则追加。
//				SmtDormitoryFloor floor = new SmtDormitoryFloor();
//				for (int i = 0; i < dormitoryFloorDTO.getFloorNum() - selectById.getFloorNum(); i++) {
//					floor.setDormitoryId(dormitoryId);
//					Integer floorNum = selectById.getFloorNum() + i + 1;
//					floor.setFloorName(floorNum);
//					floor.setRoomNum(0);
//					floor.insert();
//				}
//			} else if (selectById.getFloorNum() > dormitoryFloorDTO.getFloorNum()) {
//				SmtDormitoryFloor floor = new SmtDormitoryFloor();
//				floor.setDormitoryId(dormitoryId);
//				// 判断要删除的楼层是否存在房间，若删除的任意一层存在房间，则提示删除失败
//				for (int i = 0; i < selectById.getFloorNum() - dormitoryFloorDTO.getFloorNum(); i++) {
//					Integer floorNum = selectById.getFloorNum() - i;
//					floor.setFloorName(floorNum);
//					// 判断该楼层是否有房间
//					Integer roomCount = dormitoryMapper.queryRoomByFloor(floor);
//					if (roomCount > 0) {
//						return new Result<>(Boolean.FALSE, "该宿舍楼的第" + floorNum + "层有房间，更改失败");
//					}
//				}
//				for (int i = 0; i < selectById.getFloorNum() - dormitoryFloorDTO.getFloorNum(); i++) {
//					Integer floorNum = selectById.getFloorNum() - i;
//					floor.setFloorName(floorNum);
//					floor.delete(Wrappers.<SmtDormitoryFloor> query().lambda()
//							.eq(SmtDormitoryFloor::getDormitoryId, dormitoryId)
//							.eq(SmtDormitoryFloor::getFloorName, floorNum));
//				}
//			}
//		}
		return new Result<>(true);
	}


	@Override
	public Result removeFloorById(Integer id) {
		// TODO Auto-generated method stub

		Integer selectCount = dorStaffService.count(Wrappers.<SmtDormitoryStaff> query().lambda()
							.eq(SmtDormitoryStaff::getFloorId, id));
		if(selectCount>0)
		{
			return new Result<>(Boolean.FALSE, "该楼层已有人员入住，删除失败");
		}
		Integer roomCount= roomMapper.selectCount(Wrappers.<SmtDormitoryRoom> query().lambda()
					.eq(SmtDormitoryRoom::getFloorId, id));
		if (roomCount > 0) {
			return new Result<>(Boolean.FALSE, "该楼层有房间，删除失败");
		}

		return new Result<>(this.removeById(id));
	}


	@Override
	public Result getSmtDormitoryFloorPage(Page page, SmtDormitoryFloor smtDormitoryFloor) {
		// TODO Auto-generated method stub
		List<Integer> parkIdList = SecurityUtils.getUser().getParkIdList();
		IPage<List<FloorVO>> ipageList=mapper.getSmtDormitoryFloorPage(page,smtDormitoryFloor,parkIdList);
		return  new Result<>(ipageList);
	}


	/**
	 * 获取该楼栋的最大层数
	 */
	@Override
	public Result getFloorStartNum(Integer dormitoryId) {
		// TODO Auto-generated method stub
		Integer floorNum= mapper.selectMaxFloor(dormitoryId);
		if(floorNum!=null)
			floorNum=floorNum+1;
		return new Result<>(floorNum);
	}

}
