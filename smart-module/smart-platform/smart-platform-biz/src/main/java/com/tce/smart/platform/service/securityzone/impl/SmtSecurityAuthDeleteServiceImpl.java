package com.tce.smart.platform.service.securityzone.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.constant.enums.SmtSnapPersonEnum;
import com.tce.smart.common.core.exception.SmartException;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.util.BeanUtils;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.data.api.dto.businesstrip.resp.CcdFormtableMainRespDTO;
import com.tce.smart.data.api.dto.consume.resp.WorkTimeRespDTO;
import com.tce.smart.data.api.dto.ehrview.resp.EvwLdxRegLeaveAllRespDTO;
import com.tce.smart.data.api.dto.ehrview.resp.EvwLregLeaveAllRespDTO;
import com.tce.smart.data.api.feign.consume.RemoteRsEmpService;
import com.tce.smart.data.api.feign.ehrview.RemoteEvwLdxRegLeaveAllService;
import com.tce.smart.data.api.feign.ehrview.RemoteEvwLregLeaveAllService;
import com.tce.smart.platform.api.dto.req.securityzone.SecurityAuthDeleteReqDTO;
import com.tce.smart.platform.core.dto.SearchTravelDTO;
import com.tce.smart.platform.core.entity.*;
import com.tce.smart.platform.core.entity.securityzone.SmtSecurityAuthDelete;
import com.tce.smart.platform.core.mapper.SmtSecurityAuthDeleteMapper;
import com.tce.smart.platform.service.*;
import com.tce.smart.platform.service.securityzone.SmtSecurityAuthDeleteService;
import com.tce.smart.platform.service.securityzone.SmtSecurityWhiteService;
import com.tce.smart.tool.constant.DeviceTaskConstants;
import com.tce.smart.tool.enums.OneOrZeroEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author fushiping
 * @date 2021-07-29 11:13:24
 */
@Service
public class SmtSecurityAuthDeleteServiceImpl extends ServiceImpl<SmtSecurityAuthDeleteMapper, SmtSecurityAuthDelete> implements SmtSecurityAuthDeleteService {

	private static final Logger log = LoggerFactory.getLogger(SmtSecurityAuthDeleteServiceImpl.class);

	@Autowired
	private SmtSecurityWhiteService smtSecurityWhiteService;
	@Autowired
	private SmtStaffDeviceAuthService smtStaffDeviceAuthService;
	@Autowired
	private SmtDeviceAuthorityRelationService smtDeviceAuthorityRelationService;
	@Autowired
	private SmtSnapPersonService smtSnapPersonService;
	@Autowired
	private SmtStaffService smtStaffService;
	@Autowired
	private SmtParkBuService smtParkBuService;
	@Autowired
	private SmtOrganizeRelationService smtOrganizeRelationService;
	@Autowired
	private RemoteRsEmpService remoteRsEmpService;
	@Autowired
	private RemoteEvwLdxRegLeaveAllService remoteEvwLdxRegLeaveAllService;
	@Autowired
	private RemoteEvwLregLeaveAllService remoteEvwLregLeaveAllService;
	@Autowired
	private SmtTravelApplicationService smtTravelApplicationService;

	@Override
	public IPage<SmtSecurityAuthDelete> getList(Page page) {
		List<Integer> parkIdList = SecurityUtils.getUser().getParkIdList();
		return this.page(page, Wrappers.<SmtSecurityAuthDelete>lambdaQuery().in(SmtSecurityAuthDelete::getParkId, parkIdList));
	}

	@Override
	public SmtSecurityAuthDelete getConfig(Integer parkId) {
		SmtSecurityAuthDelete delete = this.getOne(Wrappers.<SmtSecurityAuthDelete>query().lambda().eq(SmtSecurityAuthDelete::getParkId, parkId));
		//当第一次进入配置页时，初始化一条数据
		if (Objects.isNull(delete)) {
			SmtSecurityAuthDelete newDelete = SmtSecurityAuthDelete.builder()
					.createTime(LocalDateTime.now())
					.deleteDay(OneOrZeroEnum.ZERO.getCode())
					.isBusiness(OneOrZeroEnum.ZERO.getCode())
					.isCompensatory(OneOrZeroEnum.ZERO.getCode())
					.isHoliday(OneOrZeroEnum.ZERO.getCode())
					.isLeave(OneOrZeroEnum.ZERO.getCode())
					.isWhiteList(OneOrZeroEnum.ZERO.getCode())
					.parkId(parkId).build();
			this.save(newDelete);
			return newDelete;
		}
		return delete;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Boolean editConfig(SecurityAuthDeleteReqDTO reqDTO) {
		SmtSecurityAuthDelete delete = BeanUtils.transform(SmtSecurityAuthDelete.class, reqDTO);
		if(Objects.isNull(reqDTO.getId()) && Objects.nonNull(reqDTO.getParkId())) {
			Integer count = this.count(Wrappers.<SmtSecurityAuthDelete>query().lambda().eq(SmtSecurityAuthDelete::getParkId, reqDTO.getParkId()));
			if(count > 0) {
				throw new SmartException("该园区配置已存在");
			}
		}
		this.saveOrUpdate(delete);
		return smtSecurityWhiteService.editList(reqDTO.getWhiteList(), delete.getId());
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void deleteAuthTask() {
		//查询所有园区权限自动删除策略
		List<SmtSecurityAuthDelete> deleteConfigList = this.list();
		Map<Integer, List<SmtSecurityAuthDelete>> map = deleteConfigList.stream()
				.collect(Collectors.groupingBy(SmtSecurityAuthDelete::getParkId));
		Iterator<Map.Entry<Integer, List<SmtSecurityAuthDelete>>> entries = map.entrySet().iterator();
		while (entries.hasNext()) {
			Map.Entry<Integer, List<SmtSecurityAuthDelete>> entry = entries.next();
			//园区权限删除配置
			SmtSecurityAuthDelete deleteConfig = entry.getValue().get(0);
			//判断删除逻辑是否开启
			if (Objects.isNull(deleteConfig.getDeleteDay()) || OneOrZeroEnum.ZERO.getCode().equals(deleteConfig.getDeleteDay())) {
				continue;
			}
			//根据园区查询权限，避免后续重复查询员工园区
			List<SmtStaffDeviceAuth> authRelations = smtStaffDeviceAuthService.querySecurityAuth(entry.getKey());
			if (CollUtil.isEmpty(authRelations)) {
				continue;
			}
			for (SmtStaffDeviceAuth authRelation : authRelations) {
				// 使用独立事务处理每个权限删除，避免单个失败影响全部
				deleteStaffAuthWithTransaction(authRelation, deleteConfig);
			}
		}
	}

	/**
	 * 在独立事务中删除员工权限
	 * 确保权限删除任务生成和权限关联删除的原子性
	 */
	@Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
	private void deleteStaffAuthWithTransaction(SmtStaffDeviceAuth authRelation, SmtSecurityAuthDelete deleteConfig) {
		try {
			Long staffId = authRelation.getStaffId();

			//白名单检测
			if (OneOrZeroEnum.ONE.getCode().equals(deleteConfig.getIsWhiteList())) {
				if (smtSecurityWhiteService.isExist(deleteConfig.getId(), staffId)) {
					log.debug("员工ID={}在白名单中，跳过删除", staffId);
					return;
				}
			}

			//查询该员工拥有的权限策略关联的设备列表
			List<SmtDeviceAuthorityRelation> deviceAuthList = smtDeviceAuthorityRelationService
					.list(new LambdaQueryWrapper<SmtDeviceAuthorityRelation>()
							.eq(SmtDeviceAuthorityRelation::getAuthorityId, authRelation.getAuthId()));
			if (CollUtil.isEmpty(deviceAuthList)) {
				log.warn("员工ID={}的权限ID={}没有关联设备，跳过删除", staffId, authRelation.getAuthId());
				return;
			}

			List<String> deviceIds = deviceAuthList.stream().map(SmtDeviceAuthorityRelation::getDeviceId).collect(Collectors.toList());
			//查询该员工在该设备列表上的出入记录
			List<SmtSnapPerson> snapPeople = smtSnapPersonService.list(Wrappers.<SmtSnapPerson>query().lambda()
					.eq(SmtSnapPerson::getPersonType, SmtSnapPersonEnum.SNAP_PERSON_TYPE1.getType())
					.in(SmtSnapPerson::getDeviceId, deviceIds).eq(SmtSnapPerson::getPersonId, staffId)
					.orderByDesc(SmtSnapPerson::getSnapTime));

			//取最后一次出入至今的时间差
			Boolean isDelete;
			SmtStaff staff = smtStaffService.getById(staffId);
			if (Objects.isNull(staff)) {
				log.warn("员工ID={}不存在，跳过删除", staffId);
				return;
			}

			if (CollUtil.isEmpty(snapPeople)) {
				//取第一次添加权限至今的时间差
				isDelete = this.freeDay(deleteConfig, staff.getBadge(), authRelation.getCreateTime(), new Date());
			} else {
				isDelete = this.freeDay(deleteConfig, staff.getBadge(), snapPeople.get(0).getSnapTime(), new Date());
			}

			if (!isDelete) {
				log.debug("员工ID={}未超过删除天数限制，跳过删除", staffId);
				return;
			}

			// 1. 先删除员工权限策略关联（避免重复生成删除任务）
			boolean removeResult = smtStaffDeviceAuthService.removeById(authRelation.getId());
			if (!removeResult) {
				log.error("删除员工权限策略关联失败，staffId={}, authId={}", staffId, authRelation.getAuthId());
				throw new RuntimeException("删除员工权限策略关联失败");
			}

			// 2. 生成删除人员已下发设备权限的任务
			smtStaffService.savePersonCardTask(DeviceTaskConstants.DEL,
					DateUtil.currentSeconds(), DateUtil.currentSeconds(), staff, deviceAuthList);

			log.info("成功删除员工权限，staffId={}, badge={}, authId={}, 设备数量={}",
					staffId, staff.getBadge(), authRelation.getAuthId(), deviceAuthList.size());

		} catch (Exception e) {
			log.error("删除员工权限失败，staffId={}, authId={}", authRelation.getStaffId(), authRelation.getAuthId(), e);
			// 事务会自动回滚，确保数据一致性
			throw e;
		}
	}

	/**
	 * 计算间隔中的允许空白时间
	 *
	 * @param config
	 * @return false:未超过限制时间  true:已超过限制时间
	 */
	private Boolean freeDay(SmtSecurityAuthDelete config, String badge, Date startTime, Date endTime) {
		List<DateTime> dateTimes = DateUtil.rangeToList(startTime, endTime, DateField.DAY_OF_YEAR);
		Integer initDays = dateTimes.size();
		Integer limitDays = config.getIsWhiteList();
		if (limitDays >= initDays) {
			return Boolean.FALSE;
		}
		//计算节假日
		if (OneOrZeroEnum.ONE.getCode().equals(config.getIsHoliday())) {
			//获得节假日安排
			Result<WorkTimeRespDTO> result = remoteRsEmpService.getFreeDays(badge, SecurityConstants.FROM_IN);
			if (Objects.nonNull(result.getData())) {
				//移除节假日
				dateTimes.removeAll(result.getData().getTimes());
			}
		}
		//计算出差
		if (OneOrZeroEnum.ONE.getCode().equals(config.getIsBusiness())) {
			Page page = new Page();
			page.setSize(20);
			page.setCurrent(1);
			SearchTravelDTO dto = new SearchTravelDTO();
			dto.setStaffBadge(badge);
			IPage<CcdFormtableMainRespDTO> resp = smtTravelApplicationService.getSmtTravelApplicationPage(page, dto);
			List<CcdFormtableMainRespDTO> record = resp.getRecords();
			if (CollUtil.isNotEmpty(record)) {
				//查询在计算日期内的出差记录
				List<CcdFormtableMainRespDTO> list = record.stream().filter(main -> main.getTripBeginTime().compareTo(startTime) >= 0
						|| main.getTripEndTime().compareTo(startTime) >= 0).collect(Collectors.toList());
				if (CollUtil.isNotEmpty(list)) {
					list.forEach(main -> {
						List<DateTime> tripTime = DateUtil.rangeToList(main.getTripBeginTime(), main.getTripEndTime(), DateField.DAY_OF_YEAR);
						dateTimes.removeAll(tripTime);
					});
				}
			}
		}
		//计算请假
		if (OneOrZeroEnum.ONE.getCode().equals(config.getIsLeave())) {
			Result<List<EvwLregLeaveAllRespDTO>> infoAll = remoteEvwLregLeaveAllService.info(badge, DateUtil.formatDateTime(startTime), DateUtil.formatDateTime(endTime));
			if (CollUtil.isNotEmpty(infoAll.getData())) {
				infoAll.getData().forEach(leave -> {
					List<DateTime> tripTime = DateUtil.rangeToList(leave.getBeginDate(), leave.getEndDate(), DateField.DAY_OF_YEAR);
					dateTimes.removeAll(tripTime);
				});
			}
		}
		//计算调休
		if (OneOrZeroEnum.ONE.getCode().equals(config.getIsCompensatory())) {
			Result<List<EvwLdxRegLeaveAllRespDTO>> reg = remoteEvwLdxRegLeaveAllService.listByDay(badge, DateUtil.formatDate(startTime), SecurityConstants.FROM_IN);
			if (CollUtil.isNotEmpty(reg.getData())) {
				List<DateTime> regTime = new ArrayList<>();
				reg.getData().forEach(leave -> {
					regTime.add(DateTime.of(leave.getBEGINTIME()));
				});
				dateTimes.removeAll(regTime);
			}
		}
		if (limitDays < dateTimes.size()) {
			return Boolean.TRUE;
		}
		return Boolean.FALSE;
	}

	/**
	 * 获得员工的园区ID
	 *
	 * @param staffId
	 * @return
	 */
	private Integer getPark(Long staffId) {
		SmtStaff staff = smtStaffService.getById(staffId);
		SmtParkBu bu = smtParkBuService.getOne(Wrappers.<SmtParkBu>query().lambda().eq(SmtParkBu::getCompId, staff.getCompId()));
		if (Objects.nonNull(bu)) {
			return bu.getParkId();
		}
		SmtOrganizeRelation relation = smtOrganizeRelationService.getByBu(Long.parseLong(staff.getCompId()));
		if (Objects.nonNull(relation)) {
			return relation.getParkId();
		}
		return null;
	}
}
