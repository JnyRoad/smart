package com.tce.smart.platform.service.impl.bigdatapanel;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.platform.api.dto.resp.bigdatapanel.AreaDeviceSnapRespDTO;
import com.tce.smart.platform.api.dto.resp.bigdatapanel.ParkDormitoryRespDTO;
import com.tce.smart.platform.api.dto.resp.bigdatapanel.ParkParkingRespDTO;
import com.tce.smart.platform.api.dto.resp.bigdatapanel.ParkVisitorRespDTO;
import com.tce.smart.platform.core.entity.SmtDormitoryBed;
import com.tce.smart.platform.core.entity.SmtDormitoryRoom;
import com.tce.smart.platform.core.vo.ParkingCorrectionVO;
import com.tce.smart.platform.service.*;
import com.tce.smart.platform.service.bigdatapanel.BigDataParkBasicService;
import io.swagger.models.auth.In;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @description: 大数据面板-园区总览Service实现
 * @date: 2020-08-05 14:58
 * @author: wuling
 * @version: 1.0
 */
@Service
@AllArgsConstructor
public class BigDataParkBasicServiceImpl implements BigDataParkBasicService {

	private final SmtDormitoryRoomService smtDormitoryRoomService;

	private final SmtDormitoryBedService smtDormitoryBedService;

	private final SmtSnapVehicleService smtSnapVehicleService;

	private final SmtParkingCorrectionService smtParkingCorrectionService;

	private final SmtSnapPersonService smtSnapPersonService;

	@Override
	public ParkDormitoryRespDTO getParkDormitoryInfo(Integer parkId) {
		//List<Integer> parkIdList = SecurityUtils.getUser().getParkIdList();
		//获取园区的宿舍房间总数
		int roomCount = smtDormitoryRoomService.count(new QueryWrapper<SmtDormitoryRoom>().lambda().eq(SmtDormitoryRoom::getParkId, parkId));
		//获取空闲的房间数
		Integer freeRoomCount = smtDormitoryRoomService.getFreeRoomCount(parkId);

		//获取床位总数
		int bedCount = smtDormitoryBedService.count(new QueryWrapper<SmtDormitoryBed>().lambda().eq(SmtDormitoryBed::getParkId, parkId));
		//获取空闲床位数
		Integer freeBedCount = smtDormitoryBedService.getFreeBedCount(parkId);

		return ParkDormitoryRespDTO.builder()
				.roomCount(roomCount)
				.roomFreeCount(freeRoomCount)
				.bedCount(bedCount)
				.bedFreeCount(freeBedCount)
				.build();
	}

	@Override
	public ParkParkingRespDTO getParkParkingInfo(Integer parkId) {
		//查询车位总数和空闲数
		ParkingCorrectionVO parkingCountInfo = smtParkingCorrectionService.getParkingCountInfo(parkId);
		//查询车位动态
		List<ParkParkingRespDTO.InOutRecord> inOutRecord = smtSnapVehicleService.getInOutRecord(parkId);

		return ParkParkingRespDTO.builder()
				.parkingCount(parkingCountInfo==null? 0 : parkingCountInfo.getTotalCount())
				.parkingFreeCount(parkingCountInfo==null? 0 : parkingCountInfo.getFreeCount())
				.inOutRecords(inOutRecord)
				.build();
	}

	@Override
	public ParkVisitorRespDTO getParkVisitorInfo(Integer parkId) {
		return smtSnapPersonService.getVisitorInfo(parkId);
	}

	@Override
	public List<AreaDeviceSnapRespDTO> getAreaDeviceSnapData(Integer parkId) {
		return smtSnapPersonService.getAreaDeviceSnapData(parkId);
	}
}
