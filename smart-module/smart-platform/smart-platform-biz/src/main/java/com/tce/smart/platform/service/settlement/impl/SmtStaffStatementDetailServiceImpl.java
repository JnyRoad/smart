package com.tce.smart.platform.service.settlement.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.common.core.util.BeanUtils;
import com.tce.smart.common.core.util.StringUtils;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.platform.api.dto.req.RoomMeterQueryDTO;
import com.tce.smart.platform.api.dto.req.SmtStaffStatementReqDTO;
import com.tce.smart.platform.api.dto.req.visitormanage.StaffStatementWithDorReqDTO;
import com.tce.smart.platform.api.dto.resp.commonsd.DormitorySDMeterreadNewRespDTO;
import com.tce.smart.platform.api.dto.resp.commonsd.StaffStayModifyRespDTO;
import com.tce.smart.platform.api.dto.resp.commonsd.StaffStayRespDTO;
import com.tce.smart.platform.api.dto.resp.sdstatement.SDStatementDetailRespDTO;
import com.tce.smart.platform.core.dto.SDStatementFeeDetailDTO;
import com.tce.smart.platform.core.dto.SmtStaffStatementDTO;
import com.tce.smart.platform.core.dto.SmtStaffStatementDetailDTO;
import com.tce.smart.platform.core.entity.*;
import com.tce.smart.platform.core.mapper.SmtCommonSDMapper;
import com.tce.smart.platform.core.mapper.SmtCommonSDMeterreadMapper;
import com.tce.smart.platform.core.mapper.SmtSdMeterreadDetailMapper;
import com.tce.smart.platform.core.mapper.SmtStaffStatementDetailMapper;
import com.tce.smart.platform.service.SmtDormitoryRoomService;
import com.tce.smart.platform.service.SmtDormitoryService;
import com.tce.smart.platform.service.SmtDormitoryStaffHistoryService;
import com.tce.smart.platform.service.SmtStaffService;
import com.tce.smart.platform.service.settlement.SmtSdMeterreadService;
import com.tce.smart.platform.service.settlement.SmtStaffSDMHistoryService;
import com.tce.smart.platform.service.settlement.SmtStaffStatementDetailService;
import com.tce.smart.tool.enums.MeterTypeEnum;
import com.tce.smart.tool.enums.SDCategoryEnum;
import com.tce.smart.tool.enums.SdStatementStatusEnum;
import com.tce.smart.tool.exception.TCEException;
import com.tce.smart.tool.util.ToolUtils;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.var;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @description: SmtStaffStatementDetailServiceImpl
 * @date: 2020-07-16 15:47
 * @author: wuling
 * @version: 1.0
 */
@Slf4j
@Service
@AllArgsConstructor
@RequiredArgsConstructor
public class SmtStaffStatementDetailServiceImpl extends ServiceImpl<SmtStaffStatementDetailMapper, SmtStaffStatementDetail> implements SmtStaffStatementDetailService {

	@Autowired
	private SmtStaffStatementDetailMapper smtStaffStatementDetailMapper;

	@Autowired
	private SmtStaffService smtStaffService;

	@Autowired
	private SmtSdMeterreadDetailMapper smtSdMeterreadDetailMapper;

	@Autowired
	private SmtCommonSDMeterreadMapper smtCommonSDMeterreadMapper;

	@Autowired
	private SmtCommonSDMapper smtCommonSDMapper;

	@Autowired
	private SmtStaffSDMHistoryService smtStaffSDMHistoryService;

	@Autowired
	private SmtDormitoryRoomService smtDormitoryRoomService;

	@Autowired
	private SmtDormitoryService smtDormitoryService;

	@Autowired
	private SmtStaffStatementDetailDailyServiceImpl smtStaffStatementDetailDailyService;

	@Autowired
	private SmtDormitoryStaffHistoryService smtDormitoryStaffHistoryService;

	@Value("${smart.xc-park-id:0}")
	private Integer xcParkId;

	@Override
	public IPage<SmtStaffStatementDTO> getStaffSDStatementDetail(Page page, SmtStaffStatementReqDTO smtStaffStatementReqDTO) {
		//当前登录用户所属的园区ID列表
		List<Integer> parkIdList = SecurityUtils.getUser().getParkIdList();

		SmtStaffStatementDTO smtStaffStatementDTO = new SmtStaffStatementDTO();
		BeanUtils.copyProperties(smtStaffStatementReqDTO,smtStaffStatementDTO);
		IPage<SmtStaffStatementDTO> staffSDStatementDetail = smtStaffStatementDetailMapper.getStaffSDStatementDetail(page, smtStaffStatementDTO, parkIdList);

		Map<Integer,List<SmtDormitoryRoom>> roomMap = new HashMap<>();
		if(CollectionUtil.isNotEmpty(staffSDStatementDetail.getRecords())){
			List<Integer> roomIdList = staffSDStatementDetail.getRecords().stream().map(SmtStaffStatementDTO::getRoomId).collect(Collectors.toList());
			Collection<SmtDormitoryRoom> smtDormitoryRooms = smtDormitoryRoomService.listByIds(roomIdList);
			roomMap = smtDormitoryRooms.stream().collect(Collectors.groupingBy(SmtDormitoryRoom::getId));
		}
		for(SmtStaffStatementDTO item : staffSDStatementDetail.getRecords()){
			SmtDormitoryRoom dormitoryRoom = roomMap.get(item.getRoomId()).get(0);
			if(StringUtils.isBlank(item.getName())){
				item.setName(item.getRoomInName());
			}
			item.setRoomName(StringUtils.isNotEmpty(dormitoryRoom.getAliasName()) ? dormitoryRoom.getAliasName() : dormitoryRoom.getRoomName().toString());
			if(item.getInDays() > 0) {
				item.setAvgFee(item.getFee().divide(new BigDecimal(item.getInDays()), 2, RoundingMode.HALF_UP));
			} else {
				item.setAvgFee(BigDecimal.ZERO);
			}
		}
		return staffSDStatementDetail;
	}

	@Override
	public IPage<SmtStaffStatementDTO> getStaffSDStatementDetailNew(Page page, SmtStaffStatementReqDTO smtStaffStatementReqDTO, SmtSdMeterreadService smtSdMeterreadService) {
		//当前登录用户所属的园区ID列表
		List<Integer> parkIdList = SecurityUtils.getUser().getParkIdList();

		if(null == smtStaffStatementReqDTO.getDormitoryId() && null == smtStaffStatementReqDTO.getFloorId()
				&& null == smtStaffStatementReqDTO.getRoomId()){
			throw new TCEException("请选择楼栋或楼层");
		}

		SmtStaffStatementDTO smtStaffStatementDTO = new SmtStaffStatementDTO();
		BeanUtils.copyProperties(smtStaffStatementReqDTO,smtStaffStatementDTO);

		List<SmtDormitoryRoom> roomList = smtDormitoryRoomService.list(new LambdaQueryWrapper<SmtDormitoryRoom>()
				.eq(!Objects.isNull(smtStaffStatementReqDTO.getDormitoryId()), SmtDormitoryRoom::getDormitoryId, smtStaffStatementReqDTO.getDormitoryId())
				.eq(!Objects.isNull(smtStaffStatementReqDTO.getFloorId()), SmtDormitoryRoom::getFloorId, smtStaffStatementReqDTO.getFloorId())
				.eq(!Objects.isNull(smtStaffStatementReqDTO.getRoomId()), SmtDormitoryRoom::getId, smtStaffStatementReqDTO.getRoomId())
		);

		if(CollectionUtil.isEmpty(roomList)){
			return new Page<>(page.getCurrent(),page.getSize(),0);
		}

		List<Integer> roomIds = roomList.stream().map(SmtDormitoryRoom::getId).collect(Collectors.toList());
		smtStaffStatementDTO.setRoomIds(roomIds);
		List<List<Integer>> roomIdList = new ArrayList<>();
		int count = roomIds.size() / 1000 + (roomIds.size() % 1000 > 0 ? 1 : 0);
		if (count > 0) {
			for (int i = 0; i < count; i++) {
				if (i == count - 1) {
					roomIdList.add(roomIds.subList(i * 1000, roomIds.size()));
				} else {
					roomIdList.add(roomIds.subList(i * 1000, (i + 1) * 1000));
				}
			}
		} else {
			roomIdList.add(roomIds);
		}
		IPage<SmtStaffStatementDTO> staffSDStatementDetail = smtStaffStatementDetailMapper.getStaffSDStatementDetailNew(page, smtStaffStatementDTO, parkIdList, roomIdList);
		if(CollUtil.isEmpty(staffSDStatementDetail.getRecords()) && xcParkId.equals(smtStaffStatementReqDTO.getParkId())) {
			//查询日结算表
			staffSDStatementDetail = smtStaffStatementDetailMapper.getStaffSDStatementDetailNewDaily(page, smtStaffStatementDTO, parkIdList, roomIdList);
		}
		// 批量查询本页所有工号的住房数量，避免下面 forEach 里按行循环查询数据库
		List<String> pageBadges = staffSDStatementDetail.getRecords().stream()
				.map(SmtStaffStatementDTO::getBadge).distinct().collect(Collectors.toList());
		Map<String, Integer> inRoomNumMap = smtSdMeterreadService.getInRoomNumBatch(pageBadges, smtStaffStatementReqDTO.getMeterMonth());

		staffSDStatementDetail.getRecords().forEach(item -> {
			SmtDormitoryRoom dormitoryRoom = roomList.stream().filter(s -> s.getId().equals(item.getRoomId())).findFirst().get();
			SmtDormitory dormitory = smtDormitoryService.getById(dormitoryRoom.getDormitoryId());
			if(StringUtils.isBlank(item.getName())){
				item.setName(item.getRoomInName());
			}

			item.setRoomName(StringUtils.isNotEmpty(dormitoryRoom.getAliasName()) ? dormitoryRoom.getAliasName() : dormitoryRoom.getRoomName().toString());

			if(Objects.nonNull(item.getInDays())) {
				item.setCountDays(item.getInDays() + item.getRemarkDays());
			}
			item.setFee(item.getFee().setScale(2, RoundingMode.HALF_UP));
			if(item.getInDays() > 0) {
				item.setAvgFee(item.getFee().divide(new BigDecimal(item.getInDays()), 2, RoundingMode.HALF_UP));
			} else {
				item.setAvgFee(BigDecimal.ZERO);
			}

			// 原始的按行查询在查不到任何记录时 SUM() 会返回 SQL NULL，这里用 Map#get 保留同样的“查不到就是 null”语义
			item.setInRoomNum(inRoomNumMap.get(item.getBadge()));
			item.setDormitoryName(dormitory.getDormitoryName());
			if(xcParkId.equals(item.getParkId())) {
				SmtStaffStatementDetailDaily daily = smtStaffStatementDetailDailyService.getOne(Wrappers.<SmtStaffStatementDetailDaily>query().lambda()
						.eq(SmtStaffStatementDetailDaily::getStaffBadge, item.getBadge())
						.eq(SmtStaffStatementDetailDaily::getMeterMonth, smtStaffStatementReqDTO.getMeterMonth()));
				if(Objects.nonNull(daily)) {
					item.setDailyFee(daily.getFee());
				}
			}

		});
		List<SmtStaffStatementDTO> collect = staffSDStatementDetail.getRecords().stream()
				.sorted(Comparator.comparing(SmtStaffStatementDTO::getDormitoryName).thenComparing(SmtStaffStatementDTO::getRoomName))
				.collect(Collectors.toList());
		staffSDStatementDetail.setRecords(collect);
		return staffSDStatementDetail;
	}

    @Override
    public List<SmtStaffStatementDTO> getSDMeterreadWithDor(StaffStatementWithDorReqDTO queryDTO,
															SmtSdMeterreadService smtSdMeterreadService) {
		//当前登录用户所属的园区ID列表
		List<Integer> parkIdList = SecurityUtils.getUser().getParkIdList();
		if (StringUtils.isEmpty(queryDTO.getDormitoryIds())){
			throw new TCEException("请选择楼栋");
		}
		if (Objects.isNull(queryDTO.getMeterMonth())){
			throw new TCEException("请选择抄表月份");
		}
		List<Integer> dorIds = Arrays.stream(queryDTO.getDormitoryIds().split(","))
				.map(Integer::parseInt).collect(Collectors.toList());

		List<SmtStaffStatementDTO> statementDetail = smtStaffStatementDetailMapper
				.getStaffSDStatementDetailWithDor(dorIds, queryDTO.getMeterMonth(), parkIdList);

		// 原实现对结果集里的每一行都同步循环查询一次住房数量/姓名，宿舍楼人数一多（生产上实测单次导出 1280 行）
		// 就会把接口拖到 45-50 秒、稳定超过前端 30 秒超时。这里改成先批量查询，再从内存 Map 里取值。
		List<String> badges = statementDetail.stream().map(SmtStaffStatementDTO::getBadge).distinct().collect(Collectors.toList());
		Map<String, Integer> inRoomNumMap = smtSdMeterreadService.getInRoomNumBatch(badges, queryDTO.getMeterMonth());
		List<String> blankNameBadges = statementDetail.stream()
				.filter(item -> StringUtils.isBlank(item.getName()))
				.map(SmtStaffStatementDTO::getBadge)
				.distinct().collect(Collectors.toList());
		Map<String, String> roomInNameMap = smtDormitoryStaffHistoryService.getByBadgeBatch(blankNameBadges);

		statementDetail.forEach(item -> {
			if (StringUtils.isBlank(item.getName())){
				item.setName(roomInNameMap.getOrDefault(item.getBadge(), StringUtils.EMPTY));
			}
			if (Objects.nonNull(item.getInDays())) {
				item.setCountDays(item.getInDays() + item.getRemarkDays());
			}
			item.setFee(item.getFee().setScale(2, RoundingMode.HALF_UP));
			if (item.getInDays() > 0) {
				item.setAvgFee(item.getFee().divide(new BigDecimal(item.getInDays()), 2, RoundingMode.HALF_UP));
			} else {
				item.setAvgFee(BigDecimal.ZERO);
			}
			// 原始的按行查询在查不到任何记录时 SUM() 会返回 SQL NULL，这里用 Map#get 保留同样的“查不到就是 null”语义
			item.setInRoomNum(inRoomNumMap.get(item.getBadge()));
		});
		statementDetail = statementDetail.stream().sorted(Comparator.comparing(SmtStaffStatementDTO::getDormitoryName)
				.thenComparing(SmtStaffStatementDTO::getRoomName))
				.collect(Collectors.toList());
		return statementDetail;
    }

    @Override
    public IPage<SDStatementDetailRespDTO> getStaffSDMeterRecord(Page page,Date meterMonth) {
		String staffBadge = SecurityUtils.getUser().getUsername();
		//先查询员工的结算记录数
		SmtStaffStatementReqDTO statementReqDTO = SmtStaffStatementReqDTO.builder().badge(staffBadge).build();
		if(null != meterMonth){
			statementReqDTO.setMeterMonth(meterMonth);
		}
		//按月份分组查询员工工资数据
		IPage<SmtStaffStatementDTO> staffSDStatementDetail = this.getStaffSDStatementDetail(page, statementReqDTO);
		List<Date> dateList = staffSDStatementDetail.getRecords().stream().map(SmtStaffStatementDTO::getMeterMonth).collect(Collectors.toList());
		if(CollectionUtil.isEmpty(staffSDStatementDetail.getRecords())){
			//没有水电扣费记录
			return new Page<SDStatementDetailRespDTO>();
		}
		//查询收费项目费用明细
		List<SDStatementFeeDetailDTO> sdStatementFeeDetailDTOS = this.baseMapper.getStaffSDMeterRecord(staffBadge, dateList);

		List<SDStatementDetailRespDTO> sdStatementDetailRespDTOS = new ArrayList<>();
		for(SmtStaffStatementDTO smtStaffStatementDTO : staffSDStatementDetail.getRecords()){
			SmtDormitoryRoom dormitoryRoom = smtDormitoryRoomService.getById(smtStaffStatementDTO.getRoomId());
			SDStatementDetailRespDTO sdStatementDetailRespDTO = new SDStatementDetailRespDTO();
			BeanUtils.copyProperties(smtStaffStatementDTO,sdStatementDetailRespDTO);
			sdStatementDetailRespDTO.setRoomName(StringUtils.isNotEmpty(dormitoryRoom.getAliasName()) ? dormitoryRoom.getAliasName() : dormitoryRoom.getRoomName().toString());
			sdStatementDetailRespDTO.setStaffBadge(smtStaffStatementDTO.getBadge());
			sdStatementDetailRespDTO.setStaffName(smtStaffStatementDTO.getName());
			sdStatementDetailRespDTO.setMeterMonth(smtStaffStatementDTO.getMeterMonth());
			//按月份过滤数据
			List<SDStatementFeeDetailDTO> tmpList = sdStatementFeeDetailDTOS.stream()
					.filter(a -> a.getMeterMonth().getTime()==smtStaffStatementDTO.getMeterMonth().getTime())
					.collect(Collectors.toList());
			List<SDStatementDetailRespDTO.CateInfo> cateInfos = new ArrayList<>();
			//月度总费用
			BigDecimal monthTotalFee = BigDecimal.ZERO;
			//设置扣费数据
			for(SDStatementFeeDetailDTO sdStatementFeeDetailDTO : tmpList){
				SDStatementDetailRespDTO.CateInfo cateInfo = new SDStatementDetailRespDTO.CateInfo();
				cateInfo.setFee(sdStatementFeeDetailDTO.getFee());
				cateInfo.setCateName(SDCategoryEnum.desc(sdStatementFeeDetailDTO.getCateId()));
				cateInfo.setMeterType(sdStatementFeeDetailDTO.getMeterType());
				cateInfo.setCategoryId(sdStatementFeeDetailDTO.getCateId());
				cateInfos.add(cateInfo);
				monthTotalFee = monthTotalFee.add(sdStatementFeeDetailDTO.getFee());
			}
			sdStatementDetailRespDTO.setTotalFee(monthTotalFee.setScale(2, RoundingMode.HALF_UP));
			sdStatementDetailRespDTO.setCateInfos(cateInfos);
			sdStatementDetailRespDTOS.add(sdStatementDetailRespDTO);
		}

		//构造响应数据
		IPage<SDStatementDetailRespDTO> respDTOIPage = new Page<SDStatementDetailRespDTO>(staffSDStatementDetail.getCurrent(),staffSDStatementDetail.getPages(),
				staffSDStatementDetail.getSize());
		respDTOIPage.setRecords(sdStatementDetailRespDTOS);
		return respDTOIPage;
    }


	@Override
	public List<SmtStaffStatementDetailDTO> getSDMeterreadDeteilExport(StaffStatementWithDorReqDTO staffStatementWithDorReqDTO, SmtSdMeterreadService smtSdMeterreadService) {
		List<SmtStaffStatementDetailDTO> detailDTOS = new ArrayList<>();
		//当前登录用户所属的园区ID列表
		List<Integer> parkIdList = SecurityUtils.getUser().getParkIdList();
		if (com.baomidou.mybatisplus.core.toolkit.StringUtils.isEmpty(staffStatementWithDorReqDTO.getDormitoryIds())) {
			throw new TCEException("请选择楼栋");
		}

		if (null == staffStatementWithDorReqDTO.getMeterMonth()) {
			throw new TCEException("请选择抄表月份");
		}

		List<Integer> dorIds = Arrays.asList(staffStatementWithDorReqDTO.getDormitoryIds().split(",")).stream().map(Integer::parseInt).collect(Collectors.toList());

		List<SmtDormitoryRoom> roomList = smtDormitoryRoomService.list(new LambdaQueryWrapper<SmtDormitoryRoom>()
				.in(SmtDormitoryRoom::getDormitoryId, dorIds)
		);

		if (CollectionUtil.isEmpty(roomList)) {
			throw new TCEException("房间不存在");
		}
		SmtStaffStatementDTO smtStaffStatementDTO = new SmtStaffStatementDTO();
		List<Integer> roomIds = roomList.stream().map(SmtDormitoryRoom::getId).collect(Collectors.toList());
		smtStaffStatementDTO.setRoomIds(roomIds);
		smtStaffStatementDTO.setMeterMonth(staffStatementWithDorReqDTO.getMeterMonth());

		Map<Integer, List<SmtDormitoryRoom>> roomCollect = roomList.stream().collect(Collectors.groupingBy(SmtDormitoryRoom::getId));

		//个人数据
		List<SmtStaffStatementDetailDTO> staffSDStatementDetail = smtStaffStatementDetailMapper.getStaffSDStatementDetailExport(smtStaffStatementDTO, parkIdList);
		if (CollUtil.isEmpty(staffSDStatementDetail)) {
			return detailDTOS;
		}
		//房间数据
		RoomMeterQueryDTO queryDTO = new RoomMeterQueryDTO();
		queryDTO.setDormitoryIds(ToolUtils.splitInt(staffStatementWithDorReqDTO.getDormitoryIds()));
		queryDTO.setMeterMonth(staffStatementWithDorReqDTO.getMeterMonth());
		List<DormitorySDMeterreadNewRespDTO> meter = smtSdMeterreadService.getFloorSDMeterreadNew(queryDTO);
		if (CollUtil.isEmpty(meter)) {
			return detailDTOS;
		}
		Map<Integer, List<DormitorySDMeterreadNewRespDTO>> meterCollect = meter.stream()
				.collect(Collectors.groupingBy(DormitorySDMeterreadNewRespDTO::getRoomId));

		for (SmtStaffStatementDetailDTO item : staffSDStatementDetail) {
			SmtDormitoryRoom room = roomCollect.get(item.getRoomId()).get(0);
			//入住天数
			if (Objects.nonNull(item.getInDays())) {
				item.setCountDays(item.getInDays() + item.getRemarkDays());
			}
			List<DormitorySDMeterreadNewRespDTO> dormitoryRooms = meterCollect.get(item.getRoomId());
			if (CollUtil.isEmpty(dormitoryRooms)) {
				continue;
			}
			item.setRoomName(StringUtils.isNotEmpty(room.getAliasName()) ? room.getAliasName() : room.getRoomName().toString());
			DormitorySDMeterreadNewRespDTO dormitoryRoom = dormitoryRooms.get(0);
			//未结算
			if (SdStatementStatusEnum.NON_STATEMENT.getCode().equals(dormitoryRoom.getStatementStatus())) {
				continue;
			}
			item.setDormitoryName(dormitoryRoom.getDormitoryName());
			//索引
			item.setIndex(dormitoryRoom.getDormitoryName() + dormitoryRoom.getRoomName());
			//超过费用
			item.setFee(item.getFee().setScale(2, RoundingMode.HALF_UP));
			//日平均
			item.setAvgFee(BigDecimal.valueOf(dormitoryRoom.getAvgAmount()));
			//用电金额
			Double electric = dormitoryRoom.getEleUse();
			if (Objects.nonNull(dormitoryRoom.getEleQty()) && dormitoryRoom.getEleQty() < electric) {
				electric = dormitoryRoom.getEleQty();
			}
			item.setElectric(BigDecimal.valueOf(electric * dormitoryRoom.getEleOverFee()));
			//平均
			if (item.getInDays() > 0) {
				//日平均
				BigDecimal ave = item.getElectric().divide(new BigDecimal(dormitoryRoom.getInDays()), 2, RoundingMode.HALF_UP);
				item.setRealElectric(ave.multiply(new BigDecimal(item.getInDays())));
			} else {
				item.setRealElectric(BigDecimal.ZERO);
				item.setAvgFee(BigDecimal.ZERO);
			}
			//获得水表数据
			List<SmtStaffStatementDetail> details = this.list(Wrappers.<SmtStaffStatementDetail>query().lambda()
					.eq(SmtStaffStatementDetail::getStaffBadge, item.getBadge())
					.eq(SmtStaffStatementDetail::getMeterMonth, item.getMeterMonth())
					.eq(SmtStaffStatementDetail::getRoomId, item.getRoomId())
					.eq(SmtStaffStatementDetail::getStatementStatus, SdStatementStatusEnum.STATEMENT.getCode())
					.eq(SmtStaffStatementDetail::getCategoryId, SDCategoryEnum.COLD_WATER.getCode()));
			if (CollUtil.isEmpty(details)) {
				continue;
			}
			Double water = dormitoryRoom.getColdUse();
			if (Objects.nonNull(dormitoryRoom.getColdQty()) && dormitoryRoom.getColdQty() < water) {
				water = dormitoryRoom.getColdQty();
			}
			BigDecimal sumFee = details.stream().map(SmtStaffStatementDetail::getFee).reduce(new BigDecimal(0), BigDecimal::add);
			item.setWater(BigDecimal.valueOf(water * dormitoryRoom.getColdOverFee()));
			item.setFee(item.getFee().add(sumFee.setScale(2, RoundingMode.HALF_UP)));
			//平均
			if (item.getInDays() > 0) {
				//日平均
				BigDecimal ave = item.getWater().divide(new BigDecimal(dormitoryRoom.getInDays()), 2, RoundingMode.HALF_UP);
				item.setRealWater(ave.multiply(new BigDecimal(item.getInDays())));
			} else {
				item.setRealWater(BigDecimal.ZERO);
			}
		}
		staffSDStatementDetail = staffSDStatementDetail.stream().sorted(Comparator.comparing(SmtStaffStatementDetailDTO::getRoomName).thenComparing(SmtStaffStatementDetailDTO::getRoomName))
				.collect(Collectors.toList());
		return staffSDStatementDetail;
	}


	@Transactional
	@Override
	public Boolean updateStaffStayNum(Long mrId,List<StaffStayRespDTO> staffStayRespDTOS,SmtSdMeterreadService smtSdMeterreadService) {
		//查询房间抄表记录
		SmtSdMeterread smtSdMeterread = smtSdMeterreadService.getById(mrId);
		if(null == smtSdMeterread){
			log.error("抄表记录不存在,mrId={}",mrId);
			throw new TCEException("抄表记录不存在");
		}

		if(smtSdMeterread.getStatementStatus().equals(SdStatementStatusEnum.STATEMENT.getCode())){
			log.error("抄表记录已结算，不能修改住宿天数,mrId={}",mrId);
			throw new TCEException("已结算，不能修改");
		}

		if(CollectionUtil.isEmpty(staffStayRespDTOS)){
			return false;
		}
		String username = SecurityUtils.getUser().getUsername();
		//查询员工的房间抄表记录数据
		List<SmtStaffStatementDetail> smtStaffStatementDetails = this.list(new LambdaQueryWrapper<SmtStaffStatementDetail>()
				.eq(SmtStaffStatementDetail::getMrId, mrId)
				.eq(SmtStaffStatementDetail::getMeterType,MeterTypeEnum.ROOM_METER.getCode())
		);

		//根据员工分组
		Map<String, List<StaffStayRespDTO>> stringListMap = staffStayRespDTOS.stream().collect(Collectors.groupingBy(StaffStayRespDTO::getStaffBadge));
		List<String> modifyBadge = new ArrayList<>();
		for(SmtStaffStatementDetail detail : smtStaffStatementDetails){
			StaffStayRespDTO staffStayRespDTO = stringListMap.get(detail.getStaffBadge()).get(0);
			Integer newDays = staffStayRespDTO.getTayDays();
			if(!detail.getStayDays().equals(newDays.intValue())) {
				//记录修改历史
				smtStaffSDMHistoryService.save(SmtStaffSDMHistory.builder()
						.staffBadge(detail.getStaffBadge())
						.staffName(detail.getStaffName())
						.roomId(detail.getRoomId())
						.roomName(detail.getRoomName())
						.meterMonth(detail.getMeterMonth())
						.meterType(detail.getMeterType())
						.oldDays(detail.getStayDays())
						.newDays(newDays)
						.remark(staffStayRespDTO.getRemark())
						.categoryId(detail.getCategoryId())
						.createTime(new Date())
						.userName(username)
						.build()
				);
				detail.setStayDays(newDays);
				if(!modifyBadge.contains(detail.getStaffBadge())){
					modifyBadge.add(detail.getStaffBadge());
				}
			}
		}

		if(CollectionUtil.isNotEmpty(modifyBadge)){
			Integer newTotalDay = staffStayRespDTOS.stream().mapToInt(StaffStayRespDTO::getTayDays).sum();
			//重新计算每人使用量
			List<SmtSdMeterreadDetail> meterreadDetails = smtSdMeterreadDetailMapper.selectList(new LambdaQueryWrapper<SmtSdMeterreadDetail>()
					.eq(SmtSdMeterreadDetail::getMrId, smtSdMeterread.getId())
			);
			Map<Long, List<SmtStaffStatementDetail>> collect = smtStaffStatementDetails.stream().collect(Collectors.groupingBy(SmtStaffStatementDetail::getMrdetailId));
			//按收费分类修改
			meterreadDetails.forEach(item -> {
				//修改入住总天数
				if(!item.getTotalStayDays().equals(newTotalDay)){
					item.setTotalStayDays(newTotalDay);
					smtSdMeterreadDetailMapper.updateById(item);
				}
				List<SmtStaffStatementDetail> staffStatementDetails = collect.get(item.getId());
				//修改每个人的使用量
				staffStatementDetails.forEach(det -> {
					Double realPreNum = (null != item.getRevPreMonthNum() ? item.getRevPreMonthNum() : item.getPreMonthNum());
					if(item.getTotalStayDays() > 0) {
						BigDecimal multiply = new BigDecimal(item.getCurMonthNum() - realPreNum)
								.divide(new BigDecimal(item.getTotalStayDays()), 4, RoundingMode.HALF_UP)
								.multiply(new BigDecimal(det.getStayDays()));
						det.setUsage(multiply.doubleValue());
					} else {
						det.setUsage(0.0);
					}
				});
			});
			//修改员工房间抄表记录
			this.updateBatchById(smtStaffStatementDetails);

			//计算公摊抄表记录
			modifyCommonSdStayDays(modifyBadge,smtSdMeterread.getMeterMonth(),stringListMap);
		}
		return true;
	}

	/**
	 * 修改公摊抄表人天
	 * @param modifyBadge
	 * @param meterMonth
	 * @param stringListMap
	 */
	@Transactional
	public void modifyCommonSdStayDays(List<String> modifyBadge,Date meterMonth,Map<String, List<StaffStayRespDTO>> stringListMap){
		for(String badge : modifyBadge){
			//根据员工工号和月份 查询员工的公摊抄表记录
			List<SmtStaffStatementDetail> commonStaffStatementList = this.list(new LambdaQueryWrapper<SmtStaffStatementDetail>()
					.eq(SmtStaffStatementDetail::getMeterMonth, meterMonth)
					.eq(SmtStaffStatementDetail::getStaffBadge, badge)
					.eq(SmtStaffStatementDetail::getMeterType, MeterTypeEnum.COMMON_METER.getCode())
			);

			if(CollectionUtil.isNotEmpty(commonStaffStatementList)){
				for(SmtStaffStatementDetail statementDetail : commonStaffStatementList){
					//如果存在公摊抄表记录 则所有关联的房间的员工的公摊抄表数据都需要修改
					Long commonMrId = statementDetail.getMrId();
					int oldStayDay = statementDetail.getStayDays();
					//设置新的入住天数
					StaffStayRespDTO staffStayRespDTO = stringListMap.get(statementDetail.getStaffBadge()).get(0);
					statementDetail.setStayDays(staffStayRespDTO.getTayDays());
					//查询房间列表
					SmtCommonSD smtCommonSD = smtCommonSDMapper.selectById(commonMrId);
					//该房间的员工的公摊抄表记录
					List<SmtStaffStatementDetail> commonStaffStatementDetails = this.list(new LambdaQueryWrapper<SmtStaffStatementDetail>()
							.eq(SmtStaffStatementDetail::getMeterMonth, meterMonth)
							.eq(SmtStaffStatementDetail::getRoomId, statementDetail.getRoomId())
							.eq(SmtStaffStatementDetail::getCategoryId,smtCommonSD.getCategoryId())
							.eq(SmtStaffStatementDetail::getMeterType, MeterTypeEnum.COMMON_METER.getCode())
					);

					//公摊抄表详情
					SmtCommonSDMeterread smtCommonSDMeterread = smtCommonSDMeterreadMapper.selectOne(new LambdaQueryWrapper<SmtCommonSDMeterread>()
							.eq(SmtCommonSDMeterread::getCommonId, commonMrId)
							.eq(SmtCommonSDMeterread::getMeterMonth, meterMonth)
					);

					//该房间分摊的用量
					Double preUse = smtCommonSDMeterread.getRevPreMonthNum() != null ? smtCommonSDMeterread.getRevPreMonthNum() : smtCommonSDMeterread.getPreMonthNum();
					BigDecimal roomUse = BigDecimal.valueOf(smtCommonSDMeterread.getCurMonthNum())
							.subtract(new BigDecimal(preUse))
							.divide(new BigDecimal(smtCommonSD.getRoomList().split(",").length),4, RoundingMode.HALF_UP);
					//当前总入住天数
					Integer totalDays = commonStaffStatementDetails.stream().mapToInt(SmtStaffStatementDetail::getStayDays).sum();
					totalDays += ((staffStayRespDTO.getTayDays() - oldStayDay) );
					//平均用量
					BigDecimal avgUse = roomUse.divide(new BigDecimal(totalDays),4, RoundingMode.HALF_UP);
					//遍历修改该房间的员工的公摊抄表记录
					for(SmtStaffStatementDetail detail : commonStaffStatementDetails){
						if(detail.getStaffBadge().equals(statementDetail.getStaffBadge())){
							//设置新的入住天数
							detail.setStayDays(staffStayRespDTO.getTayDays());
						}
						//设置新的用量
						detail.setUsage(new BigDecimal(detail.getStayDays()).multiply(avgUse).setScale(2, RoundingMode.HALF_UP).doubleValue());
					}
					//设置新的公摊总入住天数
					smtCommonSDMeterread.setTotalStayDays(totalDays);
					smtCommonSDMeterreadMapper.updateById(smtCommonSDMeterread);
					//批量修改员工的公摊用量数据
					this.updateBatchById(commonStaffStatementDetails);
				}
			}
		}
	}

	@Override
	public List<StaffStayModifyRespDTO> queryStaffStayUpdateRecord(Long mrId,SmtSdMeterreadService smtSdMeterreadService) {
		//查询房间抄表记录
		SmtSdMeterread sdMeterread = smtSdMeterreadService.getById(mrId);
		if(null == sdMeterread){
			log.error("抄表记录不存在,mrId={}",mrId);
			throw new TCEException("抄表记录不存在");
		}
		//查询该抄表记录关联的员工入住天数修改记录 只查电的
		List<SmtStaffSDMHistory> staffSDMHistories = smtStaffSDMHistoryService.list(new LambdaQueryWrapper<SmtStaffSDMHistory>()
				.eq(SmtStaffSDMHistory::getRoomId, sdMeterread.getRoomId())
				.eq(SmtStaffSDMHistory::getMeterMonth, sdMeterread.getMeterMonth())
				.eq(SmtStaffSDMHistory::getCategoryId,SDCategoryEnum.ELECTRIC.getCode())
		);
		List<StaffStayModifyRespDTO> modifyRespDTOS = new ArrayList<>();
		//目前修改住宿天数是作用到所有收费项目上的 展示一个收费项目记录即可
		for(SmtStaffSDMHistory item : staffSDMHistories){
			modifyRespDTOS.add(StaffStayModifyRespDTO.builder()
					.staffBadge(item.getStaffBadge())
					.staffName(item.getStaffName())
					.oldDays(item.getOldDays())
					.newDays(item.getNewDays())
					.remark(item.getRemark())
					.meterName(item.getUserName())
					.createTime(item.getCreateTime())
					.build()
			);
		}
		return modifyRespDTOS;
	}

	@Override
	public List<StaffStayRespDTO> queryStaffStayNum(Long mrId) {
		//只查询一个收费项目的数据
		List<SmtStaffStatementDetail> statementDetailList = this.list(new LambdaQueryWrapper<SmtStaffStatementDetail>()
				.eq(SmtStaffStatementDetail::getMrId, mrId)
				.eq(SmtStaffStatementDetail::getCategoryId,SDCategoryEnum.HOT_WATER.getCode())
		);
		if(CollectionUtil.isEmpty(statementDetailList)){
			//没有入住记录
			throw new TCEException("该房间没有住宿记录");
		}
		List<String> staffBadgeList = statementDetailList.stream().map(SmtStaffStatementDetail::getStaffBadge).collect(Collectors.toList());
		List<SmtStaff> smtStaffList = smtStaffService.list(new LambdaQueryWrapper<SmtStaff>().in(SmtStaff::getBadge, staffBadgeList));
		Map<String, List<SmtStaff>> collect = smtStaffList.stream().collect(Collectors.groupingBy(SmtStaff::getBadge));
		if(CollectionUtil.isEmpty(statementDetailList)){
			log.error("宿舍抄表数据不存在,mrId=",mrId);
			throw new TCEException("宿舍抄表数据不存在");
		}
		List<StaffStayRespDTO> staffStayRespDTOS = new ArrayList<>();
		statementDetailList.forEach(item -> {
			staffStayRespDTOS.add(StaffStayRespDTO.builder()
					.staffBadge(item.getStaffBadge())
					.staffName(item.getStaffName())
					.meterMonth(item.getMeterMonth())
					.inTime(item.getInTime())
					.tayDays(item.getStayDays())
					.statementDate(item.getStatementDate())
					.build());
		});

		return staffStayRespDTOS;
	}

	@Override
	public void saveStaffSdRecord(List<SmtStaffStatementDetail> smtStaffStatementDetails,Long mrId,Long mrDetailId, Integer categoryId,Date meterMonth, BigDecimal roomAvgUse) {
		//房间入住总天数
		long roomStayTotal = smtStaffStatementDetails.stream().collect(Collectors.summarizingInt(SmtStaffStatementDetail::getStayDays)).getSum();
		//房间平均用量
		BigDecimal avgUse = roomAvgUse.divide(new BigDecimal(roomStayTotal),4, RoundingMode.HALF_UP);
		//员工列表
		List<String> staffBadgeList = smtStaffStatementDetails.stream().map(SmtStaffStatementDetail::getStaffBadge).collect(Collectors.toList());
		Map<String, List<SmtStaffStatementDetail>> staffSdStatementMap = smtStaffStatementDetails.stream().collect(Collectors.groupingBy(SmtStaffStatementDetail::getStaffBadge));
		//员工水电使用数据
		List<SmtStaffStatementDetail> staffStatementDetails = this.list(new LambdaQueryWrapper<SmtStaffStatementDetail>()
				.in(SmtStaffStatementDetail::getStaffBadge, staffBadgeList)
				.eq(SmtStaffStatementDetail::getCategoryId, categoryId)
				.eq(SmtStaffStatementDetail::getMeterMonth, meterMonth)
				.eq(SmtStaffStatementDetail::getMeterType, MeterTypeEnum.COMMON_METER.getCode())
		);
		Map<String, List<SmtStaffStatementDetail>> staffSdStatementRecord = staffStatementDetails.stream().collect(Collectors.groupingBy(SmtStaffStatementDetail::getStaffBadge));

		//新增记录
		List<SmtStaffStatementDetail> addRecord = new ArrayList<>();
		//更新记录
		List<SmtStaffStatementDetail> updateRecord = new ArrayList<>();
		for(var item : staffSdStatementMap.entrySet()){
			SmtStaffStatementDetail detail = item.getValue().get(0);

			//查询该员工该房间本月的公摊抄表数据
			SmtStaffStatementDetail statementDetail = this.getOne(new LambdaQueryWrapper<SmtStaffStatementDetail>()
					.eq(SmtStaffStatementDetail::getStaffBadge, detail.getStaffBadge())
					.eq(SmtStaffStatementDetail::getCategoryId, categoryId)
					.eq(SmtStaffStatementDetail::getMeterMonth, meterMonth)
					.eq(SmtStaffStatementDetail::getMeterType, MeterTypeEnum.COMMON_METER.getCode())
					.eq(SmtStaffStatementDetail::getRoomId, detail.getRoomId())
			);
			Double useage = new BigDecimal(detail.getStayDays()).multiply(avgUse).setScale(2, RoundingMode.HALF_UP).doubleValue();
			SmtStaffStatementDetail build = SmtStaffStatementDetail.builder().build();
			BeanUtils.copyProperties(detail,build);
			build.setUsage(useage);
			if(null != statementDetail){
				SmtStaffStatementDetail oldRecord = statementDetail;
				if(SdStatementStatusEnum.STATEMENT.getCode().equals(oldRecord.getStatementStatus())){
					//已经结算 不能再修改
					log.error("{}数据已结算，不能修改",oldRecord.getStatementDate());
					throw new TCEException("已结算，不能修改");
				}
				//更新
				build.setId(oldRecord.getId());
				updateRecord.add(build);
			} else {
				//新增
				build.setMrId(mrId);
				build.setMrdetailId(mrDetailId);
				build.setCategoryId(categoryId);
				build.setMeterMonth(meterMonth);
				build.setMeterType(MeterTypeEnum.COMMON_METER.getCode());
				addRecord.add(build);
			}
		}

		if(CollectionUtil.isNotEmpty(addRecord)){
			this.saveBatch(addRecord);
		}

		if(CollectionUtil.isNotEmpty(updateRecord)){
			this.updateBatchById(updateRecord);
		}
	}
}
