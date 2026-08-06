package com.tce.smart.platform.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.common.core.constant.enums.ExceptionEnum;
import com.tce.smart.common.core.exception.SmartException;
import com.tce.smart.common.core.exception.TCEException;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.util.StringUtils;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.platform.api.dto.req.AutoAllotRoomReqDTO;
import com.tce.smart.platform.api.dto.req.DormitoryBedReqDTO;
import com.tce.smart.platform.api.dto.req.SelfCheckInReqDTO;
import com.tce.smart.platform.api.dto.req.dormitorymange.*;
import com.tce.smart.platform.api.dto.resp.*;
import com.tce.smart.platform.api.dto.resp.dormitorymange.*;
import com.tce.smart.platform.core.dto.DormitoryStaffDTO;
import com.tce.smart.platform.core.dto.dormitorymanage.*;
import com.tce.smart.platform.core.entity.*;
import com.tce.smart.platform.core.entity.ext.DormitoryRoomExt;
import com.tce.smart.platform.core.entity.ext.DormitoryStatisticsExt;
import com.tce.smart.platform.core.entity.leavecount.SmtSettlementTemplate;
import com.tce.smart.platform.core.mapper.SmtDormitoryBedMapper;
import com.tce.smart.platform.core.mapper.SmtDormitoryFloorMapper;
import com.tce.smart.platform.core.mapper.SmtDormitoryRoomMapper;
import com.tce.smart.platform.core.mapper.SmtDormitoryStaffMapper;
import com.tce.smart.platform.core.model.*;
import com.tce.smart.platform.core.vo.DormitoryCountJche;
import com.tce.smart.platform.core.vo.DormitoryRoomVO;
import com.tce.smart.platform.core.vo.RoomInfoVisualVO;
import com.tce.smart.platform.core.vo.RoomVisualVO;
import com.tce.smart.platform.service.*;
import com.tce.smart.platform.service.dormitoryconfig.SmtDormitoryPersonService;
import com.tce.smart.platform.service.leavecount.SmtSettlementTemplateRangeService;
import com.tce.smart.platform.service.leavecount.SmtSettlementTemplateService;
import com.tce.smart.platform.service.remoteLock.ConnectLockService;
import com.tce.smart.platform.service.settlement.SmtSDTemplatesService;
import com.tce.smart.tool.constant.SymbolConstants;
import com.tce.smart.tool.enums.*;
import lombok.extern.slf4j.Slf4j;
import lombok.var;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 园区宿舍楼中每个楼层的房间信息
 *
 * @author 齐佩
 * @date 2019-04-13 18:17:10
 */
@Slf4j
@Service
public class SmtDormitoryRoomServiceImpl extends ServiceImpl<SmtDormitoryRoomMapper, SmtDormitoryRoom> implements SmtDormitoryRoomService {

	@Autowired
	private SmtDormitoryRoomMapper mapper;
	@Autowired
	private SmtDormitoryStaffMapper dormitoryStaffMapper;
	@Autowired
	private SmtDormitoryFloorMapper smtDormitoryFloorMapper;
	@Autowired
	private SmtDormitoryPersonService smtDormitoryPersonService;
	@Autowired
	private SmtDormitoryStaffService smtDormitoryStaffService;
	@Autowired
	private SmtDormitoryBedService bedService;
	@Autowired
	private SmtDormitoryFloorService floorService;
	@Autowired
	private SmtStaffService smtStaffService;
	@Autowired
	private SmtSDTemplatesService smtSDTemplatesService;
	@Autowired
	private SmtDormitoryTypeService smtDormitoryTypeService;
	@Autowired
	private SmtIdCardInfoService smtIdCardInfoService;
	@Autowired
	private SmtDormitoryLevelService smtDormitoryLevelService;
	@Autowired
	private SmtOrganizeRelationService smtOrganizeRelationService;
	@Autowired
	private ConnectLockService connectLockService;
	@Autowired
	private SmtSettlementTemplateRangeService templateRangeService;
	@Autowired
	private SmtSettlementTemplateService templateService;

	@Value("${smart.xc-park-id:0}")
	private Integer xcParkId;

	@Override
	public Result getSmtDormitoryRoomPage(Page page, SmtDormitoryRoom smtDormitoryRoom) {
		// TODO Auto-generated method stub

		List<Integer> parkIdList = SecurityUtils.getUser().getParkIdList();
		IPage<DormitoryRoomVO> list = mapper.getSmtDormitoryRoomPage(page, smtDormitoryRoom, parkIdList);
		List<DormitoryRoomVO> records = list.getRecords();
		for (DormitoryRoomVO dormitoryRoomVO : records) {
			Integer staffCount = smtDormitoryStaffService.count(Wrappers.<SmtDormitoryStaff>query().lambda().eq(SmtDormitoryStaff::getRoomId, dormitoryRoomVO.getId()));
			dormitoryRoomVO.setUsedBed(staffCount);
			Integer free = dormitoryRoomVO.getBedTotal() - staffCount;
			dormitoryRoomVO.setFreeBed(free);

		}
		return new Result<>(list);
	}

	@Override
	public List<DormitoryRoomVO> getSmtDormitoryRoomList(SmtDormitoryRoom smtDormitoryRoom) {
		List<Integer> parkIdList = SecurityUtils.getUser().getParkIdList();
		List<DormitoryRoomVO> records = mapper.getSmtDormitoryRoomList(smtDormitoryRoom, parkIdList);
		for (DormitoryRoomVO dormitoryRoomVO : records) {
			Integer staffCount = smtDormitoryStaffService.count(Wrappers.<SmtDormitoryStaff>query().lambda().eq(SmtDormitoryStaff::getRoomId, dormitoryRoomVO.getId()));
			dormitoryRoomVO.setUsedBed(staffCount);
			Integer free = dormitoryRoomVO.getBedTotal() - staffCount;
			dormitoryRoomVO.setFreeBed(free);
			// 写入离职模板信息
			SmtSettlementTemplate settlementTemplate = templateService.getByRoomId(dormitoryRoomVO.getId());
			if (Objects.nonNull(settlementTemplate)) {
				dormitoryRoomVO.setLeaveTempId(settlementTemplate.getId());
				dormitoryRoomVO.setLeaveTempName(settlementTemplate.getTemplateName());
			}
		}
		return records;
	}

	@Override
	public Boolean updateDormitoryRoomById(SmtDormitoryRoom smtDormitoryRoom) {
		// TODO Auto-generated method stub
		// 房间id
		Integer roomId = smtDormitoryRoom.getId();
		SmtDormitoryRoom room = this.getById(roomId);
		if (null == room) {
			//房间不存在
			throw new TCEException("房间不存在");
		}

		List<SmtDormitoryBed> dormitoryBeds = bedService.list(new LambdaQueryWrapper<SmtDormitoryBed>().eq(SmtDormitoryBed::getRoomId, roomId));

		if (smtDormitoryRoom.getBedTotal() != null) {
			if (dormitoryBeds.size() < smtDormitoryRoom.getBedTotal()) {
				// 如果大于原始床位，则追加。
				return insertBed(smtDormitoryRoom, dormitoryBeds);
			} else if (dormitoryBeds.size() > smtDormitoryRoom.getBedTotal()) {
				return deleteBed(smtDormitoryRoom, dormitoryBeds);
			}
		}
		return this.updateById(smtDormitoryRoom);
	}

	/**
	 * 按楼层统计
	 *
	 * @param query
	 * @return
	 */
	@Override
	public List<DormitoryCountByFloor> countByFloor(FloorCountQueryReqDTO query) {
		return this.getBaseMapper().getCountByFloor(query);
	}

	/**
	 * 按性别统计
	 *
	 * @param query
	 * @return
	 */
	@Override
	public List<DormitoryCountBySex> countBySex(FloorCountQueryReqDTO query) {
		return this.getBaseMapper().getCountBySex(query);
	}

	/**
	 * 统计男、女入住人数、剩余床位数、总床位数
	 *
	 * @param parkId
	 * @return
	 */
	@Override
	public List<DormitoryCountByType> countByType(Integer parkId) {
		return this.getBaseMapper().getCountByType(parkId);
	}

	@Override
	public List<DormitoryCountFloor> countFloor(Integer dormitoryId) {
		return this.getBaseMapper().getCountFloor(dormitoryId);
	}

	@Override
	public List<DormitoryCountListRespDTO> countList(Integer parkId) {
		List<DormitoryCountList> countList = this.getBaseMapper().getCountList(parkId);
		List<DormitoryCountListRespDTO> dormitoryCountListRespDTOS = new ArrayList<>();
		countList.forEach(item -> {
			var dto = new DormitoryCountListRespDTO();
			BeanUtils.copyProperties(item, dto);
			dormitoryCountListRespDTOS.add(dto);
		});
		return dormitoryCountListRespDTOS;
	}

	@Override
	public List<DormitoryCountByBuildingRespDTO> countDormList(Integer parkId) {
		List<DormitoryCountByBuilding> dormitoryCountByBuildings = this.getBaseMapper().getCountDormList(parkId);
		List<DormitoryCountByBuildingRespDTO> dormitoryCountByBuildingRespDTOS = new ArrayList<>();
		dormitoryCountByBuildings.forEach(item -> {
			var dto = new DormitoryCountByBuildingRespDTO();
			BeanUtils.copyProperties(item, dto);
			dormitoryCountByBuildingRespDTOS.add(dto);
		});
		return dormitoryCountByBuildingRespDTOS;
	}

	@Override
	public String getDormitoryStaffHistory(Integer parkId, String certno) {
		List<SmtStaff> staffList = smtStaffService.list(Wrappers.<SmtStaff>query().lambda()
				.eq(SmtStaff::getCertno, certno).orderByDesc(SmtStaff::getCreateTime));
		if (CollUtil.isEmpty(staffList)) {
			return null;
		}
		List<SmtDormitoryStaff> staffs = smtDormitoryStaffService.list(new LambdaQueryWrapper<SmtDormitoryStaff>()
				.eq(SmtDormitoryStaff::getStaffBadge, staffList.get(0).getBadge())
				.eq(SmtDormitoryStaff::getParkId, parkId)
		);
		if (CollUtil.isNotEmpty(staffs)) {
			//石岩允许入住的宿舍楼
			List<String> dor = new ArrayList<String>() {{
				add("A栋");
				add("A栋一单元");
				add("A栋二单元");
				add("A栋三单元");
			}};
			List<SmtDormitoryStaff> syRoom = staffs.stream().filter(s -> s.getParkId() == 161 &&
					dor.contains(s.getDormitoryName())).collect(Collectors.toList());
			staffs.removeAll(syRoom);
			List<String> rooms = staffs.stream().map(SmtDormitoryStaff::getDormitoryName).collect(Collectors.toList());
			String str = StringUtils.join(",", rooms.stream().distinct().collect(Collectors.toList()));
			return "已在" + str + "入住过";
		}
		return null;
	}

	@Transactional(rollbackFor = Exception.class)
	@Override
	public List<DormitoryQuickStaffRespDTO> autoAllot(AutoAllotRoomReqDTO autoAllotRoomReqDTO, SmtDormitoryBedService smtDormitoryBedService) {
		SmtStaff smtStaff = null;
		List<SmtStaff> smtStaffList = smtStaffService.list(new LambdaQueryWrapper<SmtStaff>()
				.eq(SmtStaff::getCertno, autoAllotRoomReqDTO.getCertno())
				.ne(SmtStaff::getStatus, StaffStatusEnum.STAFF_STATUS_QUIT.getCode())
				.orderByDesc(SmtStaff::getCreateTime)
		);
		if (CollectionUtil.isNotEmpty(smtStaffList)) {
			smtStaff = smtStaffList.get(0);
		}
		if (null != smtStaff && StaffStatusEnum.STAFF_STATUS_TEMPORARY.getCode().equals(smtStaff.getStatus()) && StringUtils.isNotEmpty(smtStaff.getCompId())) {
			//查询员工BU
			Boolean excludeOrg = smtOrganizeRelationService.wasExcludeOrg(Long.parseLong(smtStaff.getCompId()));
			if (excludeOrg) {
				smtStaff = null;
			}
		}
		if (null != smtStaff) {
			if(StringUtils.isNotEmpty(autoAllotRoomReqDTO.getBadge()) && xcParkId.equals(autoAllotRoomReqDTO.getParkId())) {
				//检查级层
				List<SmtDormitoryLevel> level = smtDormitoryLevelService.getByType(autoAllotRoomReqDTO.getRoomType());
				if (CollUtil.isEmpty(level)) {
					throw new TCEException("该房间类型未关联职层，请重新选择房间类型");
				}
				List<String> levels = level.stream().map(SmtDormitoryLevel::getJcheId).collect(Collectors.toList());
				if (!levels.contains(smtStaff.getJcheId())) {
					List<String> levelName = level.stream().map(SmtDormitoryLevel::getJcheName).collect(Collectors.toList());
					throw new TCEException("该房间对应职层为" + StringUtils.join(SymbolConstants.BRANCH, levelName) + "，您职层不在范围内");
				}
			}
			int count = smtDormitoryStaffService.count(new LambdaQueryWrapper<SmtDormitoryStaff>()
					.eq(SmtDormitoryStaff::getParkId, autoAllotRoomReqDTO.getParkId())
					.eq(SmtDormitoryStaff::getStaffBadge, smtStaff.getBadge()));
			if (count > 0) {
				//已存在入住记录
				log.error("{}已存在入住记录", smtStaff.getBadge());
				throw new TCEException("已存在入住记录");
			}
		}

		if (null != autoAllotRoomReqDTO.getBedId()) {
			SmtDormitoryRoom room = this.getById(autoAllotRoomReqDTO.getRoomId());
			if(!room.getRoomSex().equals(autoAllotRoomReqDTO.getSex())) {
				throw new TCEException("房间性别属性不符合");
			}
			//指定了床位
			SmtDormitoryBed dormitoryBed = smtDormitoryBedService.getById(autoAllotRoomReqDTO.getBedId());
			if (null == dormitoryBed) {
				//床位不存在
				log.error("{}床位不存在", autoAllotRoomReqDTO.getBedId());
				throw new TCEException("床位不存在");
			}
			int count = smtDormitoryStaffService.count(new LambdaQueryWrapper<SmtDormitoryStaff>().eq(SmtDormitoryStaff::getBedId, autoAllotRoomReqDTO.getBedId()));
			if (count > 0) {
				//床位已住人
				log.error("{}床位已住人", autoAllotRoomReqDTO.getBedId());
				throw new TCEException("床位已住人");
			}
			if (null != smtStaff) {
				//存在员工信息的情况下 生成入住记录
				smtDormitoryStaffService.addDormitoryStaff(DormitoryStaffDTO.builder()
						.name(autoAllotRoomReqDTO.getName())
						.sex(autoAllotRoomReqDTO.getSex())
						.staffId(smtStaff.getId())
						.badge(smtStaff.getBadge())
						.bedId(autoAllotRoomReqDTO.getBedId())
						.compName(smtStaff.getCompName())
						.depName(smtStaff.getDepName())
						.jobName(smtStaff.getJobName())
						.build(),smtStaff.getStatus());
			} else {
				//临时人员入住的情况
				//添加员工表信息
				tempStaffAddDormitory(autoAllotRoomReqDTO, autoAllotRoomReqDTO.getBedId());
			}
			return this.getQuickStaff(autoAllotRoomReqDTO.getBedId(), dormitoryBed.getRoomId(),
					smtDormitoryBedService);
		}

		//1. 查询可待分配的房间列表
		List<SmtDormitoryRoom> smtDormitoryRooms = new ArrayList<>();
		if (null != autoAllotRoomReqDTO.getRoomId()) {
			//指定房间分配
			SmtDormitoryRoom room = this.getById(autoAllotRoomReqDTO.getRoomId());
			if (null != room) {
				smtDormitoryRooms.add(room);
			}
			if(!room.getRoomSex().equals(autoAllotRoomReqDTO.getSex())) {
				throw new TCEException("房间性别属性不符合");
			}
		} else {
			smtDormitoryRooms = this.getBaseMapper().selectList(new LambdaQueryWrapper<SmtDormitoryRoom>()
					.eq(Objects.nonNull(autoAllotRoomReqDTO.getParkId()), SmtDormitoryRoom::getParkId, autoAllotRoomReqDTO.getParkId())
					.eq(Objects.nonNull(autoAllotRoomReqDTO.getDormitoryId()), SmtDormitoryRoom::getDormitoryId, autoAllotRoomReqDTO.getDormitoryId())
					.eq(Objects.nonNull(autoAllotRoomReqDTO.getFloorId()), SmtDormitoryRoom::getFloorId, autoAllotRoomReqDTO.getFloorId())
					.eq(SmtDormitoryRoom::getRoomType, autoAllotRoomReqDTO.getRoomType())
					.eq(SmtDormitoryRoom::getRoomSex, autoAllotRoomReqDTO.getSex())
					.eq(SmtDormitoryRoom::getIsDormitoryRoom, IsDormitoryRoomEnum.YES.getCode())
					.orderByAsc(SmtDormitoryRoom::getId)
			);
		}

		if (CollectionUtil.isEmpty(smtDormitoryRooms)) {
			//没有可待分配的房间
			throw new TCEException("没有可待分配的房间");
		}
		List<Integer> rooms = smtDormitoryRooms.stream().map(SmtDormitoryRoom::getId).collect(Collectors.toList());
		//2. 查询可分配房间目前的入住情况
		List<DormitoryRoomStayDTO> dormitoryRoomStays = this.getBaseMapper().getDormitoryRoomStay(rooms);
		Map<Integer, List<DormitoryRoomStayDTO>> stringListMap = dormitoryRoomStays.stream().collect(Collectors.groupingBy(a -> a.getRoomId()));
		//hasFloor为空表示为H5端发起的入住申请，需要自动分配楼层
		boolean hasFloor = Objects.isNull(autoAllotRoomReqDTO.getFloorId()) ? Boolean.TRUE : Boolean.FALSE;
		boolean isSucc = false;
		List<DormitoryQuickStaffRespDTO> dormitoryQuickStaffRespDTOS = new ArrayList<>();

		List<DormitoryRoomExt> dormitoryRoomExtList = new ArrayList<>();
		for (SmtDormitoryRoom room : smtDormitoryRooms) {
			DormitoryRoomExt roomExt = new DormitoryRoomExt();
			BeanUtils.copyProperties(room, roomExt);

			int subCount = 0;
			if (!stringListMap.containsKey(room.getId())) {
				//当前房间没有住人
				subCount = room.getBedTotal();
			} else if (stringListMap.get(room.getId()).get(0).getActCount() < room.getBedTotal()) {
				//当前房间人未住满
				subCount = room.getBedTotal() - stringListMap.get(room.getId()).get(0).getActCount();
			} else {
				//已住满人
				continue;
			}
			roomExt.setFloorName(smtDormitoryFloorMapper.selectById(room.getFloorId()).getFloorName());
			roomExt.setFreeBedNum(subCount);
			dormitoryRoomExtList.add(roomExt);
		}

		if (CollectionUtil.isEmpty(dormitoryRoomExtList)) {
			throw new TCEException("没有空余床位");
		}
		//对房间列表按照空床位从少到多排序  hasFloor为是否需要根据楼层排序
		if (hasFloor) {
			dormitoryRoomExtList.sort(Comparator.comparing(DormitoryRoomExt::getFreeBedNum)
					.thenComparing(DormitoryRoomExt::getFloorName).thenComparing(DormitoryRoomExt::getId));
		} else {
			dormitoryRoomExtList.sort(Comparator.comparing(DormitoryRoomExt::getFreeBedNum).thenComparing(DormitoryRoomExt::getId));
		}

		//3. 比较可分配房间和入住情况 找出一个可分配入住的房间
		/**
		 * 这里要考虑一种情况
		 * 当选中一个床位时 在并发的情况下
		 * 床位可能已经被分配出去 此时应该继续考虑当前房间的下一个空闲床位
		 * 如果当前房间所有能使用的床位都出现这种情况 则继续尝试下一个可分配房间
		 */
		for (DormitoryRoomExt roomExt : dormitoryRoomExtList) {
			//当前房间的空余床位数
			int subCount = roomExt.getFreeBedNum();

			for (int i = 0; i < subCount; i++) {
				//找到一个空床铺
				SmtDormitoryBed smtDormitoryBed = getUseBed(roomExt);
				if (null == smtDormitoryBed) {
					continue;
				}
				if (null != smtStaff) {
					//存在员工信息的情况下 生成入住记录
					try {
						smtDormitoryStaffService.addDormitoryStaff(DormitoryStaffDTO.builder()
								.name(autoAllotRoomReqDTO.getName())
								.sex(autoAllotRoomReqDTO.getSex())
								.staffId(smtStaff.getId())
								.badge(smtStaff.getBadge())
								.bedId(smtDormitoryBed.getId())
								.compName(smtStaff.getCompName())
								.depName(smtStaff.getDepName())
								.jobName(smtStaff.getJobName())
								.build(),smtStaff.getStatus());
					} catch (Exception e) {
						log.error(e.getMessage());
						continue;
					}
					//生成开门动态码
					try {
						connectLockService.generatePwdByBadge(smtStaff.getBadge());
					}catch (Exception e) {
						log.error(e.getMessage());
					}
				} else {
					//临时人员入住的情况
					//添加员工表信息
					tempStaffAddDormitory(autoAllotRoomReqDTO, smtDormitoryBed.getId());
				}
				dormitoryQuickStaffRespDTOS = this.getQuickStaff(smtDormitoryBed.getId(), smtDormitoryBed.getRoomId(), smtDormitoryBedService);
				if(StringUtils.isNotEmpty(autoAllotRoomReqDTO.getSignOrg())) {
					//记录身份证信息
					SmtIdCardInfo cardInfo = smtIdCardInfoService.getById(autoAllotRoomReqDTO.getCertno());
					SmtIdCardInfo idCardInfo = SmtIdCardInfo.builder()
							.id(autoAllotRoomReqDTO.getCertno())
							.name(autoAllotRoomReqDTO.getName())
							.nation(autoAllotRoomReqDTO.getNation())
							.sex(autoAllotRoomReqDTO.getSex())
							.birthday(autoAllotRoomReqDTO.getBirthday())
							.validDateStart(autoAllotRoomReqDTO.getValidDateStart())
							.validDateEnd(autoAllotRoomReqDTO.getValidDateEnd())
							.signOrg(autoAllotRoomReqDTO.getSignOrg())
							.address(autoAllotRoomReqDTO.getAddress())
							.build();
					if (null == cardInfo) {
						//添加
						smtIdCardInfoService.save(idCardInfo);
					} else {
						//修改
						smtIdCardInfoService.updateById(idCardInfo);
					}
				}
				isSucc = true;
				break;
			}

			if (isSucc) {
				//已找到床位
				break;
			}
		}
		if(CollUtil.isEmpty(dormitoryQuickStaffRespDTOS)) {
			throw new SmartException("无可用床位");
		}
		return dormitoryQuickStaffRespDTOS;
	}

	@Override
	public List<DormitoryQuickStaffRespDTO> autoAllotForAuthenticatedStaff(String badge,
			SelfCheckInReqDTO request, SmtDormitoryBedService smtDormitoryBedService) {
		SmtStaff staff = smtStaffService.getActiveStaffByBadge(badge);
		if (staff == null || StrUtil.isBlank(staff.getCertno()) || !Objects.equals(badge, staff.getBadge())) {
			throw new AccessDeniedException("当前认证用户没有可用的员工身份");
		}

		AutoAllotRoomReqDTO serverRequest = new AutoAllotRoomReqDTO();
		serverRequest.setParkId(request.getParkId());
		serverRequest.setDormitoryId(request.getDormitoryId());
		serverRequest.setFloorId(request.getFloorId());
		serverRequest.setRoomId(request.getRoomId());
		serverRequest.setBedId(request.getBedId());
		serverRequest.setRoomType(request.getRoomType());
		// 员工身份和性别一律以服务端在职档案为准，不能相信浏览器传值。
		serverRequest.setBadge(staff.getBadge());
		serverRequest.setCertno(staff.getCertno());
		serverRequest.setName(staff.getName());
		serverRequest.setSex(staff.getSex());
		return autoAllot(serverRequest, smtDormitoryBedService);
	}

	@Override
	public DormitoryQuickStaffRespDTO printInfo(Long recordId) {
		return null;
	}

	List<DormitoryQuickStaffRespDTO> getQuickStaff(Integer bedId, Integer roomId,
												   SmtDormitoryBedService smtDormitoryBedService) {
		List<SearchDormitoryRoomDetailRespDTO.BedDetail> dormitoryBeds = smtDormitoryBedService.getBedDetail(roomId);
		List<DormitoryQuickStaffRespDTO> respDTOS = new ArrayList<>();
		if (CollUtil.isEmpty(dormitoryBeds)) {
			return null;
		}
		SmtDormitoryStaff smtDormitoryStaff = dormitoryStaffMapper.getBedInfo(bedId);
		SmtDormitoryRoom dormitoryRoom = this.getById(roomId);
		dormitoryBeds.forEach(bedDetail -> {
			DormitoryQuickStaffRespDTO quickStaff = new DormitoryQuickStaffRespDTO();
			quickStaff.setId(bedDetail.getDorStaffId());
			quickStaff.setRoomName(StringUtils.isNotEmpty(dormitoryRoom.getAliasName()) ? dormitoryRoom.getAliasName() : dormitoryRoom.getRoomName().toString());
			quickStaff.setDormitoryName(smtDormitoryStaff.getDormitoryName());
			quickStaff.setBedNumber(bedDetail.getBedNumber());
			quickStaff.setCreateTime(bedDetail.getInTime());
			quickStaff.setDepName(bedDetail.getStaffInfo().getDepName());
			quickStaff.setDorJobName(bedDetail.getStaffInfo().getJobName());
			quickStaff.setName(bedDetail.getStaffInfo().getStaffName());
			quickStaff.setRoomId(roomId);
			quickStaff.setBedId(bedDetail.getBedId());
			quickStaff.setSex(bedDetail.getStaffInfo().getSex());
			quickStaff.setStaffBadge(bedDetail.getStaffInfo().getStaffBadge());
			quickStaff.setIsFlag(OneOrZeroEnum.ZERO.getCode());
			if (bedId.equals(bedDetail.getBedId())) {
				quickStaff.setIsFlag(OneOrZeroEnum.ONE.getCode());
			}
			respDTOS.add(quickStaff);
		});
		return respDTOS;
	}

	/**
	 * 临时人员入住
	 * 注意
	 * 这里单独开启一个事务 不能影响调用方法的事务流程
	 * 但是调用方法异常时 该方法也要回滚
	 * 所以该方法的事务应该是调用方法事务的子事务
	 *
	 * @param autoAllotRoomReqDTO
	 * @param bedId
	 * @return
	 */
	@Transactional(propagation = Propagation.NESTED)
	public Boolean tempStaffAddDormitory(AutoAllotRoomReqDTO autoAllotRoomReqDTO, Integer bedId) {
		Long staffId;
		SmtStaff staff;
		List<SmtStaff> reSmtStaffs = smtStaffService.list(new LambdaQueryWrapper<SmtStaff>()
				.eq(SmtStaff::getCertno, autoAllotRoomReqDTO.getCertno())
				.eq(SmtStaff::getStatus, StaffStatusEnum.STAFF_STATUS_QUIT.getCode())
				.orderByDesc(SmtStaff::getCreateTime));
		if (CollUtil.isNotEmpty(reSmtStaffs)) {
			SmtStaff reSmtStaff = reSmtStaffs.get(0);
			smtStaffService.update(Wrappers.<SmtStaff>update().lambda()
					.set(SmtStaff::getBadge, autoAllotRoomReqDTO.getCertno())
					.set(SmtStaff::getWelfareLevel, "")
					.set(SmtStaff::getJcheId, "")
					.set(SmtStaff::getDepId, "")
					.set(SmtStaff::getCompId, "")
					.set(SmtStaff::getAge, 0)
					.set(SmtStaff::getStatus, StaffStatusEnum.UNKNOWN.getCode())
					.set(SmtStaff::getApplicationId, 0)
					.set(SmtStaff::getDepAbbr, "")
					.set(SmtStaff::getCompName, "")
					.set(SmtStaff::getDepName, "")
					.set(SmtStaff::getDispatch, "")
					.set(SmtStaff::getDormitoryStatus, DormitoryStatusEnum.NOT_INNER.getCode())
					.set(SmtStaff::getEId, 0)
					.set(SmtStaff::getEmail, "")
					.set(SmtStaff::getEmpType, 0)
					.set(SmtStaff::getFacePicId, "")
					.set(SmtStaff::getHomeAddress, "")
					.set(SmtStaff::getNation, "")
					.set(SmtStaff::getPzid, 0)
					.set(SmtStaff::getPolice, "")
					.set(SmtStaff::getReportTo, "")
					.set(SmtStaff::getResidentaddress, "")
					.set(SmtStaff::getSeqId, 0)
					.set(SmtStaff::getSex, 0)
					.set(SmtStaff::getWechat, "")
					.set(SmtStaff::getLiveAddress, "")
					.eq(SmtStaff::getId, reSmtStaff.getId()));
			staffId = reSmtStaff.getId();
			staff = reSmtStaff;
		} else {
			//添加员工表信息
			SmtStaff newSmtStaff = new SmtStaff();
			newSmtStaff.setBadge(autoAllotRoomReqDTO.getCertno());      //以身份证代替工号 当正式入职时 再以身份证为标识更新为正式员工数据
			newSmtStaff.setCertno(autoAllotRoomReqDTO.getCertno());
			newSmtStaff.setName(autoAllotRoomReqDTO.getName());
			newSmtStaff.setSex(autoAllotRoomReqDTO.getSex());
			newSmtStaff.setStatus(StaffStatusEnum.UNKNOWN.getCode());
			newSmtStaff.setCreateTime(new Date());
			smtStaffService.save(newSmtStaff);
			staffId = newSmtStaff.getId();
			staff = newSmtStaff;
		}
		//添加入住记录
		smtDormitoryStaffService.addDormitoryStaffTemp(DormitoryStaffDTO.builder()
				.name(autoAllotRoomReqDTO.getName())
				.staffId(staffId)
				.badge(autoAllotRoomReqDTO.getCertno())
				.sex(autoAllotRoomReqDTO.getSex())
				.bedId(bedId)
				.build());
		//生成开门动态码
		try {
			//TODO 智能锁注释
			/*connectLockService.generatePwdByBadge(staff.getBadge());*/
		}catch (Exception e) {
			log.error(e.getMessage());
		}
		return true;
	}

	@Override
	public DormitoryDistRespDTO recommendBed(DormitoryDistReqDTO distReqDTO) {
		//查询员工信息
		SmtStaff staff = smtStaffService.getOne(new LambdaQueryWrapper<SmtStaff>()
				.eq(SmtStaff::getBadge, distReqDTO.getStaffBadge()));
		if (null == staff) {
			throw new TCEException("员工不存在");
		} else if (StaffStatusEnum.STAFF_STATUS_QUIT.getCode().equals(staff.getStatus())) {
			throw new TCEException("员工已离职");
		}
		//查询职层对应房间类型
		List<SmtDormitoryLevel> dormitoryLevels = smtDormitoryLevelService.list(new LambdaQueryWrapper<SmtDormitoryLevel>()
				.eq(	SmtDormitoryLevel::getJcheId, staff.getJcheId()));
		if (CollectionUtil.isEmpty(dormitoryLevels)) {
			throw new TCEException("没有对应房间类型");
		}

		//查询类型对应的所有房间
		List<Integer> typeList = dormitoryLevels.stream().map(a -> a.getDormitoryTypeId()).collect(Collectors.toList());
		List<SmtDormitoryRoom> roomList = this.list(new LambdaQueryWrapper<SmtDormitoryRoom>()
				.eq(SmtDormitoryRoom::getParkId, distReqDTO.getParkId())
				.eq(SmtDormitoryRoom::getRoomSex, staff.getSex())
				.eq(SmtDormitoryRoom::getIsDormitoryRoom, IsDormitoryRoomEnum.YES.getCode())
				.in(SmtDormitoryRoom::getRoomType, typeList));
		if (CollectionUtil.isEmpty(roomList)) {
			throw new TCEException("没有可用房间");
		}

		List<Integer> roomIds = roomList.stream().map(SmtDormitoryRoom::getId).collect(Collectors.toList());

		//2. 查询可分配房间目前的入住情况
		List<DormitoryRoomStayDTO> dormitoryRoomStays = this.getBaseMapper().getDormitoryRoomStay(roomIds);
		Map<Integer, List<DormitoryRoomStayDTO>> stringListMap = dormitoryRoomStays.stream().collect(Collectors.groupingBy(a -> a.getRoomId()));

		SmtDormitoryBed smtDormitoryBed = null;
		//遍历可用房间 查找可用床位
		for (SmtDormitoryRoom room : roomList) {
			if (!stringListMap.containsKey(room.getId()) || stringListMap.get(room.getId()).get(0).getActCount() < room.getBedTotal()) {
				//如果该房间没有住人 或者 人没有住满 则可以分配入住
				//找到一个空床铺
				smtDormitoryBed = getUseBed(room);
				if (null != smtDormitoryBed) {
					break;
				}
			}
		}
		if (null != smtDormitoryBed) {
			SmtDormitoryStaff dormitoryStaff = dormitoryStaffMapper.getBedInfo(smtDormitoryBed.getId());
			//分配到一个床位
			return DormitoryDistRespDTO.builder()
					.parkId(dormitoryStaff.getParkId())
					.parkName(dormitoryStaff.getParkName())
					.dormitoryId(dormitoryStaff.getDormitoryId())
					.dormitoryName(dormitoryStaff.getDormitoryName())
					.floorId(dormitoryStaff.getFloorId())
					.floorName(dormitoryStaff.getFloorName())
					.roomId(dormitoryStaff.getRoomId())
					.roomName(dormitoryStaff.getRoomName())
					.roomTypeId(dormitoryStaff.getDormitoryTypeId())
					.roomTypeName(dormitoryStaff.getDormitoryTypeName())
					.bedId(dormitoryStaff.getBedId())
					.bedName(smtDormitoryBed.getBedName())
					.staffBadge(staff.getBadge())
					.staffName(staff.getName())
					.build();
		}

		return null;
	}

	@Override
	public List<DormitoryRoomDetailRespDTO> bedDetail(Integer roomId) {
		//查询房间所有床位
		List<SmtDormitoryBed> dormitoryBeds = bedService.list(new LambdaQueryWrapper<SmtDormitoryBed>()
				.eq(SmtDormitoryBed::getRoomId, roomId)
				.orderByAsc(SmtDormitoryBed::getId)
		);
		if (CollectionUtil.isEmpty(dormitoryBeds)) {
			//没有床位信息
			log.error("房间{}没有床位信息", roomId);
			throw new TCEException("没有床位信息");
		}
		//查询房间当前入住记录
		Map<String, List<SmtStaff>> stringListMap = new HashMap<>();
		Map<Integer, List<SmtDormitoryStaff>> listMap = new HashMap<>();
		List<SmtDormitoryStaff> dormitoryStaffs = smtDormitoryStaffService.list(new LambdaQueryWrapper<SmtDormitoryStaff>().eq(SmtDormitoryStaff::getRoomId, roomId));
		if (CollectionUtil.isNotEmpty(dormitoryStaffs)) {
			listMap = dormitoryStaffs.stream().collect(Collectors.groupingBy(a -> a.getBedId()));
			List<String> badges = dormitoryStaffs.stream().filter(o -> StrUtil.isNotBlank(o.getStaffBadge()))
					.map(SmtDormitoryStaff::getStaffBadge).collect(Collectors.toList());
			//根据工号列表查询员工记录
			List<SmtStaff> smtStaffs = smtStaffService.list(new LambdaQueryWrapper<SmtStaff>().in(SmtStaff::getBadge, badges));
			stringListMap = smtStaffs.stream().collect(Collectors.groupingBy(a -> a.getBadge()));
		}

		List<DormitoryRoomDetailRespDTO> dormitoryRoomDetailRespDTOS = new ArrayList<>();
		for (SmtDormitoryBed item : dormitoryBeds) {
			DormitoryRoomDetailRespDTO build = DormitoryRoomDetailRespDTO.builder()
					.id(item.getId())
					.bedNumber(item.getBedName())
					.delFlag(item.getDelFlag())
					.build();
			if (listMap.containsKey(item.getId())) {
				SmtDormitoryStaff tmpDor = listMap.get(item.getId()).get(0);
				//该床位有人住
				build.setInRecordId(tmpDor.getId());
				build.setStaffBadge(tmpDor.getStaffBadge());
				build.setStaffName(tmpDor.getStaffName());
				build.setInDate(tmpDor.getCreateTime());
				build.setRoomId(tmpDor.getRoomId());

				if (stringListMap.containsKey(tmpDor.getStaffBadge())) {
					//存在员工信息时
					SmtStaff tmpStaff = stringListMap.get(tmpDor.getStaffBadge()).get(0);
					build.setStatus(tmpStaff.getStatus());
					build.setSex(tmpStaff.getSex());
					build.setDepName(tmpStaff.getDepName());
					build.setJobName(tmpStaff.getJobName());
				}
				//不存在员工信息 即临时入住或先入住再入职的情况
			}
			dormitoryRoomDetailRespDTOS.add(build);
		}
		return dormitoryRoomDetailRespDTOS;
	}

	@Override
	public List<FloorRoomListRespDTO> getFloorRoomList(Integer dormitoryId) {
		List<FloorRoomListDTO> floorRoomList = this.baseMapper.getFloorRoomList(dormitoryId);
		List<FloorRoomListRespDTO> floorRoomListRespDTOS = new ArrayList<>();
		if (null != floorRoomList) {
			Map<Integer, List<FloorRoomListDTO>> collect = floorRoomList.stream().collect(Collectors.groupingBy(t -> t.getFloorId()));

			for (var item : collect.entrySet()) {
				FloorRoomListRespDTO floorRoomListRespDTO = new FloorRoomListRespDTO();
				floorRoomListRespDTO.setFloorId(item.getValue().get(0).getFloorId());
				floorRoomListRespDTO.setFloorName(item.getValue().get(0).getFloorName());
				List<FloorRoomListRespDTO.Room> rooms = new ArrayList<>();
				item.getValue().forEach(val -> {
					FloorRoomListRespDTO.Room room = new FloorRoomListRespDTO.Room();
					room.setRoomId(val.getRoomId());
					room.setRoomName(val.getRoomName());
					rooms.add(room);
				});
				floorRoomListRespDTO.setRoomList(rooms);
				floorRoomListRespDTOS.add(floorRoomListRespDTO);
			}
		}
		return floorRoomListRespDTOS;
	}

	@Override
	public List<FloorRoomListRespDTO> getRoomListByFloors(List<Integer> floors) {
		List<SmtDormitoryRoom> roomList = this.list(new LambdaQueryWrapper<SmtDormitoryRoom>().in(SmtDormitoryRoom::getFloorId, floors));
		if (CollectionUtil.isEmpty(roomList)) {
			log.error("楼层{}不存在房间列表", JSONUtil.toJsonStr(floors));
			throw new TCEException("不存在房间列表");
		}

		Map<Integer, List<SmtDormitoryRoom>> listMap = roomList.stream().collect(Collectors.groupingBy(t -> t.getFloorId()));
		List<FloorRoomListRespDTO> floorRoomListRespDTOS = new ArrayList<>();
		for (var item : listMap.entrySet()) {
			FloorRoomListRespDTO floorRoomListRespDTO = new FloorRoomListRespDTO();
			floorRoomListRespDTO.setFloorId(item.getKey());
			List<FloorRoomListRespDTO.Room> rooms = new ArrayList<>();

			item.getValue().forEach(r -> {
				FloorRoomListRespDTO.Room room = new FloorRoomListRespDTO.Room();
				room.setRoomId(r.getId());
				room.setRoomName(r.getRoomName());
				rooms.add(room);
			});

			floorRoomListRespDTO.setRoomList(rooms);
			floorRoomListRespDTOS.add(floorRoomListRespDTO);

		}

		return floorRoomListRespDTOS;
	}

	@Override
	public List<DormitoryRoomRespDTO> getRoomListByCondition(DormitoryRoomReqDTO dormitoryRoomReqDTO) {
		String badge = SecurityUtils.getUser().getUsername();
		//获取人员职层
		SmtDormitoryRoom searchRoom = new SmtDormitoryRoom();
		BeanUtils.copyProperties(dormitoryRoomReqDTO, searchRoom);
		SmtStaff staff = smtStaffService.getSimpleSttaffByBadge(badge);
		List<Integer> dormitoryTypeIds = new ArrayList<>();
		if(Objects.nonNull(staff)) {
			List<SmtDormitoryLevel> levels = smtDormitoryLevelService.getByJcheId(staff.getJcheId());
			dormitoryTypeIds = levels.stream().map(SmtDormitoryLevel::getDormitoryTypeId).collect(Collectors.toList());
			searchRoom.setRoomSex(staff.getSex());
		}
		List<DormitoryRoomExt> roomListByCondition = this.baseMapper.getRoomListByCondition(searchRoom, dormitoryRoomReqDTO.getFreeBedNum(), dormitoryTypeIds);
		List<DormitoryRoomRespDTO> roomRespDTOS = new ArrayList<>();
		roomListByCondition.forEach(item -> {
			DormitoryRoomRespDTO roomRespDTO = new DormitoryRoomRespDTO();
			BeanUtils.copyProperties(item, roomRespDTO);
			roomRespDTO.setRoomName(StringUtils.isNotEmpty(item.getAliasName()) ? item.getAliasName() : item.getRoomName().toString());
			roomRespDTO.setFreeBedNum(item.getFreeBedNum());
			roomRespDTO.setRoomId(item.getId());
			roomRespDTOS.add(roomRespDTO);
		});
		return roomRespDTOS;
	}

	@Override
	public List<DormitoryStatisticsListRespDTO> getRoomStatistics(FloorStatisticsQueryReqDTO queryReqDTO) {
		List<Integer> dormitoryIds = smtDormitoryPersonService.getDormitoryId(SecurityUtils.getUser().getUsername(), null);
		if (CollUtil.isEmpty(queryReqDTO.getDormitoryId())) {
			queryReqDTO.setDormitoryId(dormitoryIds);
		}
		if(StringUtils.isNotEmpty(queryReqDTO.getJcheId())) {
			List<SmtDormitoryLevel> jcheId = smtDormitoryLevelService.getByJcheId(queryReqDTO.getJcheId());
			if(CollUtil.isEmpty(jcheId)) {
				return null;
			}
			List<Integer> typeId = jcheId.stream().map(SmtDormitoryLevel::getDormitoryTypeId).collect(Collectors.toList());
			queryReqDTO.setRoomType(typeId);
		}
		List<DormitoryStatisticsExt> ext = this.baseMapper.getRoomStatistics(queryReqDTO);
		if (CollUtil.isNotEmpty(ext)) {
			List<DormitoryStatisticsRespDTO> statisticsRespDTOS =
					com.tce.smart.common.core.util.BeanUtils.batchTransform(DormitoryStatisticsRespDTO.class, ext);
			List<DormitoryStatisticsListRespDTO> dtoList = new ArrayList<>();
			Map<Integer, List<DormitoryStatisticsRespDTO>> map = statisticsRespDTOS.stream()
					.collect(Collectors.groupingBy(DormitoryStatisticsRespDTO::getRoomSex));
			Iterator<Map.Entry<Integer, List<DormitoryStatisticsRespDTO>>> entries = map.entrySet().iterator();
			while (entries.hasNext()) {
				Map.Entry<Integer, List<DormitoryStatisticsRespDTO>> entry = entries.next();
				DormitoryStatisticsListRespDTO resp = new DormitoryStatisticsListRespDTO();
				resp.setRoomSex(entry.getKey());
				resp.setRoomSexDesc(DormitorySexEnum.desc(entry.getKey()));
				resp.setSexList(entry.getValue());
				dtoList.add(resp);
			}
			return dtoList;
		}
		return null;
	}


	/**
	 * 找出一个空闲的床位
	 *
	 * @param room
	 * @return
	 */
	private SmtDormitoryBed getUseBed(SmtDormitoryRoom room) {

		//该房间的所有床位
		List<SmtDormitoryBed> smtDormitoryBeds = bedService.list(new LambdaQueryWrapper<SmtDormitoryBed>()
				.eq(SmtDormitoryBed::getParkId, room.getParkId())
				.eq(SmtDormitoryBed::getDormitoryId, room.getDormitoryId())
				.eq(SmtDormitoryBed::getRoomId, room.getId())
				.eq(SmtDormitoryBed::getDelFlag, DeleteStatusEnum.NOT_DELETE.getCode())
				.orderByAsc(SmtDormitoryBed::getId)
		);
		//该房间正在入住的床位
		List<SmtDormitoryStaff> smtDormitoryStaffs = smtDormitoryStaffService.list(new LambdaQueryWrapper<SmtDormitoryStaff>().eq(SmtDormitoryStaff::getRoomId, room.getId()));
		List<Integer> bedIds = smtDormitoryStaffs.stream().map(SmtDormitoryStaff::getBedId).collect(Collectors.toList());

		for (SmtDormitoryBed smtDormitoryBed : smtDormitoryBeds) {
			if (!bedIds.contains(smtDormitoryBed.getId())) {
				return smtDormitoryBed;
			}
		}

		return null;
	}

	@Override
	public List<DormitoryCountBuilding> countBuilding(Integer parkId) {
		List<Integer> dormirotyIds = smtDormitoryPersonService.getDormitoryId(SecurityUtils.getUser().getUsername(), null);
		return this.getBaseMapper().getCountBuilding(parkId, dormirotyIds);
	}


	/**
	 * 修改的房间的床位数大于原始床位数
	 *
	 * @param smtDormitoryRoom
	 * @return
	 */
	@Transactional
	public Boolean insertBed(SmtDormitoryRoom smtDormitoryRoom, List<SmtDormitoryBed> dormitoryBeds) {
		Integer roomId = smtDormitoryRoom.getId();
		SmtDormitoryRoom selectById = this.getById(roomId);
		//获取床位编号最大的元素
		int maxNum = 1;
		if (CollectionUtil.isNotEmpty(dormitoryBeds)) {
			SmtDormitoryBed smtDormitoryBed = dormitoryBeds.stream().max(Comparator.comparing(SmtDormitoryBed::getBedNumber)).get();
			maxNum = smtDormitoryBed.getBedNumber();
		}

		// 如果大于原始床位，则追加。
		List<SmtDormitoryBed> addBed = new ArrayList<>();
		//这里不能使用房间的床位数来比较 因为房间的床位数不包含锁定的床位
		for (int i = 0; i < smtDormitoryRoom.getBedTotal() - dormitoryBeds.size(); i++) {
			Integer bedNum = dormitoryBeds.size() + i + 1;

			SmtDormitoryBed bed = new SmtDormitoryBed();
			bed.setRoomId(roomId);
			bed.setParkId(selectById.getParkId());
			bed.setDormitoryId(selectById.getDormitoryId());
			bed.setFloorId(selectById.getFloorId());
			bed.setBedNumber(bedNum);
			bed.setBedName(String.valueOf(bedNum));
			addBed.add(bed);
		}

		bedService.saveBatch(addBed);

		//新的床位数量需要减去已锁定的数量 已锁定的床位数=房间原床位总数-房间原可用床位数
		smtDormitoryRoom.setBedTotal(smtDormitoryRoom.getBedTotal() - (dormitoryBeds.size() - selectById.getBedTotal()));

		return this.updateById(smtDormitoryRoom);
	}

	/**
	 * 修改的房间的床位数小于原始床位数
	 *
	 * @param smtDormitoryRoom
	 * @return
	 */
	@Transactional
	public Boolean deleteBed(SmtDormitoryRoom smtDormitoryRoom, List<SmtDormitoryBed> dormitoryBeds) {
		Integer roomId = smtDormitoryRoom.getId();
		//需要删除的床位数
		final int needDelNum = dormitoryBeds.size() - smtDormitoryRoom.getBedTotal();

		//已入住的记录
		List<SmtDormitoryStaff> dormitoryStaffs = smtDormitoryStaffService.list(new LambdaQueryWrapper<SmtDormitoryStaff>().eq(SmtDormitoryStaff::getRoomId, roomId));
		List<Integer> inBedIds = dormitoryStaffs.stream().map(SmtDormitoryStaff::getBedId).collect(Collectors.toList());

		if (dormitoryBeds.size() - dormitoryStaffs.size() < needDelNum) {
			//床位总数(包括标记为不可用的床位) - 已入住的床位 小于 需要删除的床位数
			//注意这里不能使用房间的床位数来减  因为房间的床位数不包含不可用的床位
			throw new TCEException("修改失败，空余床位数不够");
		}

		//已禁用的床位 已禁用的床位一定是未住人的
		List<SmtDormitoryBed> delCollectBed = dormitoryBeds.stream().filter(s -> DeleteStatusEnum.IS_DELETE.getCode().equals(s.getDelFlag())).collect(Collectors.toList());
		//按床位编号倒序排
		delCollectBed = delCollectBed.stream().sorted(Comparator.comparing(SmtDormitoryBed::getBedNumber).reversed()).collect(Collectors.toList());


		SmtDormitoryBed bed = new SmtDormitoryBed();
		bed.setRoomId(roomId);


		//已删除的床位数
		List<Integer> delBedIds = new ArrayList<>();
		//先从已禁用的床位删起 从床位编号大的开始删
		for (int i = 0; i < delCollectBed.size() && delBedIds.size() < needDelNum; i++) {
			delBedIds.add(delCollectBed.get(i).getId());
		}

		if (delBedIds.size() < needDelNum) {
			//被禁用的床位数不够删 继续删除正常的未住人的床位 从编号大的开始删
			//正常的未使用的床位
			List<SmtDormitoryBed> freeBeds = dormitoryBeds.stream().filter(s -> DeleteStatusEnum.NOT_DELETE.getCode().equals(s.getDelFlag()) && !inBedIds.contains(s.getId())).collect(Collectors.toList());
			//按床位编号倒序排
			freeBeds = freeBeds.stream().sorted(Comparator.comparing(SmtDormitoryBed::getBedNumber).reversed()).collect(Collectors.toList());

			for (int i = 0; delBedIds.size() < needDelNum; i++) {
				delBedIds.add(freeBeds.get(i).getId());
			}
		}

		//删除床位
		Integer rowIn = ((SmtDormitoryBedMapper) bedService.getBaseMapper()).delBedByIds(delBedIds);
		if (rowIn != delBedIds.size()) {
			//删除的数量不一致
			throw new TCEException("修改床位数失败");
		}

		//房间新的床位数=房间新的床位总数-房间剩余的锁定床位数 房间剩余的锁定床位数=房间原锁定床位-本次删除的床位数 如果房间剩余的锁定床位数计算出来为负数 则置为0
		smtDormitoryRoom.setBedTotal(smtDormitoryRoom.getBedTotal() - (delCollectBed.size() - needDelNum > 0 ? delCollectBed.size() - needDelNum : 0));

		return this.updateById(smtDormitoryRoom);
	}

	@Override
	public List<RoomBedRespDTO> queryRoom(SearchDormitoryRoomDetailReqDTO roomDetailReqDTO) {
		List<RoomBedRespDTO> respDTOS = new ArrayList<>();
		List<SmtDormitoryRoom> roomList = mapper.queryRoom(roomDetailReqDTO.getFloorId());
		for (SmtDormitoryRoom smtDormitoryRoom : roomList) {
			RoomBedRespDTO respDTO = new RoomBedRespDTO();
			respDTO.setId(smtDormitoryRoom.getId());
			respDTO.setRoomName(smtDormitoryRoom.getRoomName());
			respDTO.setBedTotal(smtDormitoryRoom.getBedTotal());
			respDTO.setDormitoryId(smtDormitoryRoom.getDormitoryId());
			respDTO.setFloorId(smtDormitoryRoom.getFloorId());
			respDTO.setIsDormitoryRoom(smtDormitoryRoom.getIsDormitoryRoom());
			respDTO.setRoomNum(smtDormitoryRoom.getRoomNum());
			respDTO.setRoomSex(smtDormitoryRoom.getRoomSex());
			respDTO.setRoomType(smtDormitoryRoom.getRoomType());
			respDTO.setParkId(smtDormitoryRoom.getParkId());

			DormitoryBedReqDTO reqDTO = new DormitoryBedReqDTO();
			reqDTO.setRoomId(smtDormitoryRoom.getId());
			Result<List<SmtDormitoryBed>> result = bedService.queryBed(reqDTO);
			if (result.isSuccess()) {
				List<RoomBedRespDTO.Bed> bedList = new ArrayList<>();
				List<SmtDormitoryBed> beds = result.getData();
				for (SmtDormitoryBed smtDormitoryBed : beds) {
					RoomBedRespDTO.Bed bed = new RoomBedRespDTO.Bed();
					bed.setBedId(smtDormitoryBed.getId());
					bed.setBedName(smtDormitoryBed.getBedNumber());
					bedList.add(bed);
				}
				respDTO.setBeds(bedList);
			}
			respDTOS.add(respDTO);
		}
		return respDTOS;
	}

	@Override
	public List<SmtDormitoryRoom> queryRoomList(SmtDormitoryRoom smtDormitoryRoom) {
		QueryWrapper<SmtDormitoryRoom> queryWrapper = new QueryWrapper<>();
		queryWrapper.lambda()
				.eq(SmtDormitoryRoom::getParkId, smtDormitoryRoom.getParkId())
				.eq(SmtDormitoryRoom::getDormitoryId, smtDormitoryRoom.getDormitoryId())
				.eq(SmtDormitoryRoom::getFloorId, smtDormitoryRoom.getFloorId())
				.orderByAsc(SmtDormitoryRoom::getRoomName);
		return mapper.selectList(queryWrapper);
	}


	/**
	 * 删除房间
	 */
	@Override
	public Result removeRoomById(Integer id) {
		// TODO Auto-generated method stub

		Integer selectCount2 = smtDormitoryStaffService.count(Wrappers.<SmtDormitoryStaff>query().lambda().eq(SmtDormitoryStaff::getRoomId, id));
		if (selectCount2 > 0) {
			return new Result<>(Boolean.FALSE, "该房间已有人员入住，删除失败");
		}
		Integer selectCount = bedService.count(Wrappers.<SmtDormitoryBed>query().lambda().eq(SmtDormitoryBed::getRoomId, id));
		if (selectCount > 0) {
			return new Result<>(Boolean.FALSE, "该房间已有床位，删除失败");
		}

		SmtDormitoryRoom selectById = this.getById(id);
		SmtDormitoryFloor byId = floorService.getById(selectById.getFloorId());
		/*int deleteById = */
		mapper.deleteById(id);
		Integer newRoomNum = byId.getRoomNum() - 1;
		byId.setRoomNum(newRoomNum);
		//删除房间后，更新该楼层的房间个数
		floorService.updateById(byId);
		return new Result<>(true);
	}

	@Override
	public Result getRoomVisual(String floorList) {
		// TODO Auto-generated method stub

		List<RoomVisualVO> vo = new ArrayList<>();
		if (floorList != null) {
			String object = JSONUtil.parseObj(floorList).get("floorList").toString();
			JSONArray parseArray = JSONUtil.parseArray(object);
			List<SmtDormitoryFloor> list = JSONUtil.toList(parseArray, SmtDormitoryFloor.class);
			for (SmtDormitoryFloor floor : list) {
				RoomVisualVO roomVo = new RoomVisualVO();
				roomVo.setFloorId(floor.getId());
				roomVo.setFloorName(floor.getFloorName());
				List<RoomInfoVisualVO> roomInfo = mapper.getRoomVisual(floor.getId());
				roomVo.setRoomInfoList(roomInfo);
				vo.add(roomVo);
			}
		}

		return new Result<>(vo);
	}

	@Override
	public Page<SearchDormitoryRoomDetailRespDTO> queryRoomVisual(SearchDormitoryRoomDetailReqDTO searchDormitoryRoomDetailReqDTO) {
		List<Integer> parkIdList = smtDormitoryPersonService.getParkId(SecurityUtils.getUser().getUsername());
		if (CollUtil.isEmpty(parkIdList)) {
			parkIdList = SecurityUtils.getUser().getParkIdList();
		}
		SearchDormitoryRoomDetailDTO searchDormitoryRoomDetailDTO = new SearchDormitoryRoomDetailDTO();
		BeanUtils.copyProperties(searchDormitoryRoomDetailReqDTO, searchDormitoryRoomDetailDTO);
		searchDormitoryRoomDetailDTO.setDormitoryIds(smtDormitoryPersonService.getDormitoryId(SecurityUtils.getUser().getUsername(), null));
		//按条件查询房间列表
		IPage<DormitoryRoomDetailDTO> roomVisualPage = this.getBaseMapper().getRoomVisualPage(new Page(searchDormitoryRoomDetailReqDTO.getCurrent(),
				searchDormitoryRoomDetailReqDTO.getSize()), searchDormitoryRoomDetailDTO, parkIdList);
		List<DormitoryRoomDetailDTO> detailDTOS = roomVisualPage.getRecords();

		Page<SearchDormitoryRoomDetailRespDTO> page = new Page<>(roomVisualPage.getCurrent(), roomVisualPage.getSize(), roomVisualPage.getTotal());

		List<SearchDormitoryRoomDetailRespDTO> respDTOS = new ArrayList<>();

		for (DormitoryRoomDetailDTO detailDTO : detailDTOS) {
			var item = new SearchDormitoryRoomDetailRespDTO();
			BeanUtils.copyProperties(detailDTO, item);
			if (item.getActCount().intValue() < item.getBedTotal().intValue()) {
				item.setInStatus(RoomInStatusEnum.NON_FULL.getCode());
			} else if (item.getActCount().intValue() == item.getBedTotal().intValue()) {
				item.setInStatus(RoomInStatusEnum.FULL.getCode());
			} else {
				item.setInStatus(RoomInStatusEnum.EMPTY.getCode());
			}
			//查询房间的入住详情
			List<SearchDormitoryRoomDetailRespDTO.BedDetail> bedDetails = bedService.getBedDetail(detailDTO.getRoomId());
			item.setBedDetailList(bedDetails);

			respDTOS.add(item);
		}

		page.setRecords(respDTOS);

		return page;
	}

	@Override
	public List<DormitoryCountJche> countByjche(FloorCountQueryReqDTO query) {
		// TODO Auto-generated method stub
		return this.getBaseMapper().getCountByJche(query);
	}

	@Override
	public List<DormitoryRoomExt> countByRoomType(Integer parkId) {
		return this.baseMapper.getCountByRoomType(parkId);
	}

	@Override
	public List<DormitoryCountByFloor> countByFree(FloorCountQueryReqDTO query) {
		// TODO Auto-generated method stub
		return this.getBaseMapper().getCountByFree(query);
	}

	@Override
	public Integer getJcheFreeBed(Integer parkId, String badge) {

		SmtStaff one = smtStaffService.getOne(Wrappers.<SmtStaff>query().lambda().eq(SmtStaff::getBadge, badge));
		if (ObjectUtil.isNull(one)) {
			throw new TCEException("员工不存在");
		}
		Integer count = mapper.getJcheToalBed(parkId, one.getJcheId(), one.getSex());
		Integer userCount = mapper.getJcheUseBed(parkId, one.getJcheId(), one.getSex());
		Integer free = count - userCount;
		if (free <= 0) {
			free = 0;
		}
		return free;
	}

	@Override
	public Integer batchUpdateSDTemp(DormitoryRoomAttrDTO dormitoryRoomAttrDTO) {

		Assert.notNull(dormitoryRoomAttrDTO.getParkId(), "园区不能为NULL");
		Assert.notNull(dormitoryRoomAttrDTO.getSdTemplateId(), "水电模板不能为NULL");

		//判断水电模板是否对应园区
		List<SmtSdTemplates> tempList = smtSDTemplatesService.getSDTempByParkId(dormitoryRoomAttrDTO.getParkId());
		if (tempList == null || !tempList.stream().anyMatch(a -> a.getId().equals(dormitoryRoomAttrDTO.getSdTemplateId()))) {
			throw new TCEException("园区和水电模板不匹配");
		}

		UpdateWrapper<SmtDormitoryRoom> updateWrapper = new UpdateWrapper<>();
		updateWrapper.lambda()
				.in(SmtDormitoryRoom::getId, dormitoryRoomAttrDTO.getRoomIds());

		return mapper.update(SmtDormitoryRoom.builder()
						.sdTemplateId(dormitoryRoomAttrDTO.getSdTemplateId())
						.build(),
				updateWrapper);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Integer batchUpdateRoomAttr(SmtDormitoryRoom smtDormitoryRoom) {
		Assert.notNull(smtDormitoryRoom.getParkId(), "园区不能为NULL");

		//查询房间列表
		List<SmtDormitoryRoom> smtDormitoryRooms = this.list(new LambdaQueryWrapper<SmtDormitoryRoom>()
				.eq(SmtDormitoryRoom::getParkId, smtDormitoryRoom.getParkId())
				.eq(smtDormitoryRoom.getDormitoryId() != null, SmtDormitoryRoom::getDormitoryId, smtDormitoryRoom.getDormitoryId())
				.eq(smtDormitoryRoom.getFloorId() != null, SmtDormitoryRoom::getFloorId, smtDormitoryRoom.getFloorId()));
//				.groupBy(SmtDormitoryRoom::getRoomName)
//				.select(SmtDormitoryRoom::getRoomName));

		int modNum = 0;
		if (null != smtDormitoryRoom.getRoomType()) {
			//查询房间类型对应的房间数
			SmtDormitoryType dormitoryType = smtDormitoryTypeService.getById(smtDormitoryRoom.getRoomType());
			modNum = dormitoryType.getBedTotal();
		}

		for (SmtDormitoryRoom room : smtDormitoryRooms) {
			room.setRoomSex(smtDormitoryRoom.getRoomSex());
			room.setIsDormitoryRoom(smtDormitoryRoom.getIsDormitoryRoom());
			room.setRoomType(smtDormitoryRoom.getRoomType());
			if (null != smtDormitoryRoom.getRoomType()) {
				room.setBedTotal(modNum);
				updateDormitoryRoomById(room);
			} else {
				//不需要修改床位数 直接修改房间属性
				this.updateById(room);
			}
		}

		return smtDormitoryRooms.size();
	}

	@Override
	@Transactional
	public Integer batchUpdateRoomAttrByIds(DormitoryRoomAttrDTO dormitoryRoomAttrDTO) {
		//查询房间列表
		List<SmtDormitoryRoom> smtDormitoryRooms = this.list(new LambdaQueryWrapper<SmtDormitoryRoom>()
				.in(SmtDormitoryRoom::getId, dormitoryRoomAttrDTO.getRoomIds()));

		int modNum = 0;
		if (null != dormitoryRoomAttrDTO.getRoomType()) {
			//查询房间类型对应的房间数
			SmtDormitoryType dormitoryType = smtDormitoryTypeService.getById(dormitoryRoomAttrDTO.getRoomType());
			modNum = dormitoryType.getBedTotal();
		}

		for (SmtDormitoryRoom room : smtDormitoryRooms) {
			room.setIsCount(dormitoryRoomAttrDTO.getIsCount());
			room.setRoomSex(dormitoryRoomAttrDTO.getRoomSex());
			room.setIsDormitoryRoom(dormitoryRoomAttrDTO.getIsDormitoryRoom());
			room.setRoomType(dormitoryRoomAttrDTO.getRoomType());
			if (null != dormitoryRoomAttrDTO.getRoomType()) {
				room.setBedTotal(modNum);
				updateDormitoryRoomById(room);
			} else {
				//不需要修改床位数 直接修改房间属性
				this.updateById(room);
			}
		}

		return dormitoryRoomAttrDTO.getRoomIds().size();
	}

	@Override
	public Integer getFreeRoomCount(Integer parkId) {
		//List<Integer> parkIdList = SecurityUtils.getUser().getParkIdList();
		Integer freeRoomCount = this.baseMapper.getFreeRoomCount(parkId);
		return freeRoomCount;
	}

	@Override
	public Boolean decrementBedNum(Integer roomId) {
		return this.baseMapper.decrementBedNum(roomId);
	}

	@Override
	public Boolean incrementBedNum(Integer roomId) {
		return this.baseMapper.incrementBedNum(roomId);
	}

	@Override
	public SmtDormitoryRoom getByDormitoryAndName(Integer dormitoryId, Integer roomName) {
		return this.getOne(Wrappers.<SmtDormitoryRoom>lambdaQuery()
				.eq(SmtDormitoryRoom::getDormitoryId, dormitoryId)
				.eq(SmtDormitoryRoom::getRoomName, roomName)
		);
	}

	@Override
	public ResponseEntity<byte[]> getRoomStatisticsExcel(FloorStatisticsQueryReqDTO queryReqDTO) {
		List<DormitoryStatisticsListRespDTO> ext = this.getRoomStatistics(queryReqDTO);
		Workbook workbook = new XSSFWorkbook();
		Sheet sheet = null;
		try {
			sheet = workbook.createSheet("床位监控按楼层统计表");
		} catch (Exception e) {
			e.printStackTrace();
		}
		Row row0 = sheet.createRow(0);
		String[] headers = {"序号", "宿舍名称", "宿舍分类", "可用房间", "标配人数", "标配床位", "锁定床位", "可住人数", "已住人数", "空床位", "单独空房间"};
		for (int i = 0; i < headers.length; i++) {
			Cell cell = row0.createCell(i);
			cell.setCellValue(headers[i]);
		}
		int index = 1;
		if (CollUtil.isEmpty(ext)) {
			return null;
		}
		for (DormitoryStatisticsListRespDTO list : ext) {
			List<DormitoryStatisticsRespDTO> sexList = list.getSexList();
			int num = 1;
			for (int i = 0; i < sexList.size(); i++) {
				if (Objects.isNull(sexList.get(i).getTypeBedTotal())) {
					sexList.get(i).setTypeBedTotal(0);
				}
				Row row1 = sheet.createRow(index);
				DormitoryStatisticsRespDTO sex = sexList.get(i);
				Cell cell0 = row1.createCell(0);
				cell0.setCellValue(num);
				Cell cell1 = row1.createCell(1);
				cell1.setCellValue(sex.getDormitoryDesc());
				Cell cell2 = row1.createCell(2);
				cell2.setCellValue(sex.getRoomTypeDesc());
				Cell cell3 = row1.createCell(3);
				cell3.setCellValue(sex.getRoomNum());
				Cell cell4 = row1.createCell(4);
				cell4.setCellValue(sex.getTypeBedTotal());
				Cell cell5 = row1.createCell(5);
				cell5.setCellValue(sex.getStandardBedNum());
				Cell cell6 = row1.createCell(6);
				cell6.setCellValue(sex.getLockBedNum());
				Cell cell7 = row1.createCell(7);
				cell7.setCellValue(sex.getRoomBedTotal());
				Cell cell8 = row1.createCell(8);
				cell8.setCellValue(sex.getAlreadyUse());
				Cell cell9 = row1.createCell(9);
				cell9.setCellValue(sex.getFreeBedNum());
				Cell cell10 = row1.createCell(10);
				cell10.setCellValue(sex.getFreeRoomNum());
				index++;
				num++;
			}
			CellStyle style = workbook.createCellStyle();
			style.setAlignment(HorizontalAlignment.CENTER);
			style.setVerticalAlignment(VerticalAlignment.CENTER);
			style.setFillForegroundColor(IndexedColors.LIGHT_ORANGE.getIndex());
			style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
			Row row1 = sheet.createRow(index);
			Cell cell0 = row1.createCell(0);
			cell0.setCellValue("宿舍-" + list.getRoomSexDesc());
			cell0.setCellStyle(style);
			row1.createCell(1).setCellStyle(style);
			row1.createCell(2).setCellStyle(style);
			Cell cell3 = row1.createCell(3);
			cell3.setCellStyle(style);
			cell3.setCellValue(sexList.stream().mapToInt(DormitoryStatisticsRespDTO::getRoomNum).sum());
			Cell cell4 = row1.createCell(4);
			cell4.setCellValue(sexList.stream().mapToInt(DormitoryStatisticsRespDTO::getTypeBedTotal).sum());
			cell4.setCellStyle(style);
			Cell cell5 = row1.createCell(5);
			cell5.setCellStyle(style);
			cell5.setCellValue(sexList.stream().mapToInt(DormitoryStatisticsRespDTO::getStandardBedNum).sum());
			Cell cell6 = row1.createCell(6);
			cell6.setCellValue(sexList.stream().mapToInt(DormitoryStatisticsRespDTO::getLockBedNum).sum());
			cell6.setCellStyle(style);
			Cell cell7 = row1.createCell(7);
			cell7.setCellStyle(style);
			cell7.setCellValue(sexList.stream().mapToInt(DormitoryStatisticsRespDTO::getRoomBedTotal).sum());
			Cell cell8 = row1.createCell(8);
			cell8.setCellValue(sexList.stream().mapToInt(DormitoryStatisticsRespDTO::getAlreadyUse).sum());
			cell8.setCellStyle(style);
			Cell cell9 = row1.createCell(9);
			cell9.setCellValue(sexList.stream().mapToInt(DormitoryStatisticsRespDTO::getFreeBedNum).sum());
			cell9.setCellStyle(style);
			Cell cell10 = row1.createCell(10);
			cell10.setCellValue(sexList.stream().mapToInt(DormitoryStatisticsRespDTO::getFreeRoomNum).sum());
			cell10.setCellStyle(style);
			sheet.addMergedRegion(new CellRangeAddress(index, index, 0, 2));
			index++;
		}
		String downFile = "楼层统计.xls";
		return getExcelResp(downFile, workbook);
	}


	public static ResponseEntity<byte[]> getExcelResp(String fileName, Workbook workbook) {
		ResponseEntity<byte[]> responseEntity;
		try (ByteArrayOutputStream outByteStream = new ByteArrayOutputStream()) {
			workbook.write(outByteStream);
			String attachmentName = URLEncoder.encode(fileName, "UTF-8");
			final HttpHeaders httpHeaders = new HttpHeaders();
			httpHeaders.setContentDispositionFormData("attachment", attachmentName);
			httpHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
			responseEntity = new ResponseEntity<>(outByteStream.toByteArray(), httpHeaders, HttpStatus.OK);
		} catch (IOException e) {
			log.error("下载异常", e);
			throw new SmartException(ExceptionEnum.UNKNOWN.getCode(), "下载异常");
		}
		return responseEntity;
	}


}
