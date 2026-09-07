package com.tce.smart.platform.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.common.core.constant.enums.SmtVisitorEnum;
import com.tce.smart.common.core.exception.SmartException;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.platform.dto.authoperation.AuthOperationReceipt;
import com.tce.smart.platform.dto.authoperation.AuthOperationIntakeCommand;
import com.tce.smart.platform.dto.authoperation.AuthOperationIntakeReceipt;
import com.tce.smart.platform.dto.authoperation.AuthOperationIntakeCapability;
import com.tce.smart.platform.api.dto.req.AreaTypeSwitchReqDTO;
import com.tce.smart.platform.api.dto.req.AuthDetailQueryDTO;
import com.tce.smart.platform.api.dto.req.DeviceAuthRelationAddReqDTO;
import com.tce.smart.platform.api.dto.req.DeviceAuthRelationDelReqDTO;
import com.tce.smart.platform.api.dto.resp.AreaTypeConflictDeviceVO;
import com.tce.smart.platform.api.dto.resp.AreaTypeSwitchRespDTO;
import com.tce.smart.platform.api.dto.resp.AuthDetailRespDTO;
import com.tce.smart.platform.core.dto.SmtDeviceAuthorityDTO;
import com.tce.smart.platform.core.dto.VehicleAuthDTO;
import com.tce.smart.platform.core.entity.*;
import com.tce.smart.platform.core.enums.StaffSyncEnum;
import com.tce.smart.platform.core.mapper.SmtDeviceAuthorityMapper;
import com.tce.smart.platform.core.mapper.SmtDeviceMapper;
import com.tce.smart.platform.core.mapper.SmtVehicleMapper;
import com.tce.smart.platform.core.model.DeviceTree;
import com.tce.smart.platform.core.service.SmtDeviceService;
import com.tce.smart.platform.core.service.SmtDeviceTaskService;
import com.tce.smart.platform.core.service.SmtIscDeviceTaskService;
import com.tce.smart.platform.core.service.impl.SmtIscDeviceTaskServiceImpl;
import com.tce.smart.platform.core.service.SmtBatchDeviceTaskService;
import com.tce.smart.platform.core.util.DeviceAuthorityChangesCalculator;
import com.tce.smart.platform.core.util.PermissionValidityWindow;
import com.tce.smart.platform.core.vo.DeviceAuthorityVO;
import com.tce.smart.platform.service.*;
import com.tce.smart.tool.constant.DeviceTaskConstants;
import com.tce.smart.tool.constant.SymbolConstants;
import com.tce.smart.tool.enums.*;
import com.tce.smart.tool.util.OracleInBatchUtils;
import com.tce.smart.tool.util.ToolUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 设备权限表
 *
 * @author 王艳勇
 * @date 2019-04-15 15:15:34
 */
@Slf4j
@Service
@lombok.RequiredArgsConstructor
public class SmtDeviceAuthorityServiceImpl extends ServiceImpl<SmtDeviceAuthorityMapper, SmtDeviceAuthority> implements SmtDeviceAuthorityService {
	@org.springframework.beans.factory.annotation.Autowired(required=false)
	private EmployeeAuthOperationAdapter employeeAuthOperationAdapter;
    @org.springframework.beans.factory.annotation.Autowired
    private EmployeeAuthIntakeService employeeAuthIntakeService;
	private final SmtDeviceService smtDeviceService;
	private final SmtDeviceAuthorityMapper smtDeviceAuthorityMapper;
	private final SmtDeviceAuthorityRelationService smtDeviceAuthorityRelationService;
	private final SmtBusinessDeviceAuthService smtBusinessDeviceAuthService;

	private final SmtStaffDeviceAuthService smtStaffDeviceAuthService;

	private final SmtDeviceMapper smtDeviceMapper;

	private final SmtDeviceTaskService smtDeviceTaskService;

	private final SmtIscDeviceTaskService smtIscDeviceTaskService;

	private final SmtVehicleApplyService smtVehicleApplyService;

	private final SmtStaffService smtStaffService;

	private final SmtVehicleMapper smtVehicleMapper;
	private final SmtIscDeviceTaskServiceImpl smtIscDeviceTaskServiceImpl;
	private final SmtBatchDeviceTaskService smtBatchDeviceTaskService;


	/**
	 * 查询设备权限信息
	 *
	 * @param page   分页对象
	 * @param entity 查询条件
	 * @return 返回设备权限集合
	 */
	@Override
	public IPage getDeviceAuthority(Page page, SmtDeviceAuthority entity) {
		return smtDeviceAuthorityMapper.getDeviceAuthority(page, entity);
	}

	@Override
	public List<SmtDeviceAuthority> getByStaffId(String staffId) {
		return baseMapper.getByStaffId(staffId);
	}

	/**
	 * 添加设备权限信息
	 *
	 * @param entity 设备权限信息
	 * @return 返回结果
	 */
	@Override
	public boolean saveDeviceAuthority(SmtDeviceAuthorityDTO entity) {
        if(employeeAuthOperationAdapter!=null)employeeAuthOperationAdapter.checkNewAuthority(entity.getParkId(),entity.getCheckedlimits()==null?Collections.emptyList():Arrays.asList(entity.getCheckedlimits()));
		entity.setCreateTime(LocalDateTime.now());
		boolean result = this.save(entity);
		if (result) {
			if (ArrayUtil.isNotEmpty(entity.getCheckedlimits())) {
				for (String deviceId : entity.getCheckedlimits()) {
					this.addDeviceAuthority(deviceId, entity.getId());
				}
			}
		}
		return result;
	}

	private void addDeviceAuthority(String deviceId, Integer id) {
		SmtDevice smtDevice = smtDeviceService.getById(deviceId);
		SmtDeviceAuthorityRelation deviceAuthorityRelation = new SmtDeviceAuthorityRelation();
		deviceAuthorityRelation.setDeviceId(deviceId);
		deviceAuthorityRelation.setAuthorityId(id);
		deviceAuthorityRelation.setParkId(smtDevice.getParkId());
		smtDeviceAuthorityRelationService.save(deviceAuthorityRelation);
	}

	/**
	 * 更新设备权限信息 - 性能优化版本
	 * 优化点：
	 * 1. 使用高效的设备差异计算算法
	 * 2. 批量查询和批量插入，减少数据库交互次数
	 * 3. 精确更新，只处理真正变更的设备
	 * 4. 添加性能监控日志
	 * 5. 修复新建权限时设备关联关系为空的问题
	 *
	 * @param entity 设备权限信息
	 * @param parkIds 园区ID列表
	 * @return 返回结果
	 */
	@Transactional
	@Override
	public boolean updateDeviceAuthority(SmtDeviceAuthorityDTO entity, List<Integer> parkIds) {
		if(employeeAuthOperationAdapter!=null){
            Integer verifiedPark=employeeAuthOperationAdapter.guardedAuthorityPark(entity.getId(),entity.getParkId(),parkIds);
            Boolean accepted=employeeAuthOperationAdapter.authorityDevices(entity.getId(),entity.getCheckedlimits()==null?Collections.emptyList():Arrays.asList(entity.getCheckedlimits()));
            if(accepted!=null){entity.setParkId(verifiedPark);this.updateById(entity);updateDeviceRelationsBatch(entity.getId(),Collections.singletonList(verifiedPark),entity.getCheckedlimits()==null?Collections.emptyList():Arrays.asList(entity.getCheckedlimits()));return accepted;}
        }
		long startTime = System.currentTimeMillis();
		log.info("开始更新设备权限: 权限ID={}, 新设备数量={}", entity.getId(),
				entity.getCheckedlimits() != null ? entity.getCheckedlimits().length : 0);

		// 1. 获取当前设备关联关系
		List<SmtDeviceAuthorityRelation> currentRelations = smtDeviceAuthorityRelationService.list(
			new LambdaQueryWrapper<SmtDeviceAuthorityRelation>()
				.eq(SmtDeviceAuthorityRelation::getAuthorityId, entity.getId())
				.in(SmtDeviceAuthorityRelation::getParkId, parkIds)
				.orderByAsc(SmtDeviceAuthorityRelation::getId)
		);

		// 2. 提取当前设备ID列表（可能为空列表，但不影响后续处理）
		List<String> oldDeviceIds = currentRelations.stream()
			.map(SmtDeviceAuthorityRelation::getDeviceId)
			.collect(Collectors.toList());

		// 3. 计算设备差异（使用优化的算法）
		DeviceAuthorityChangesCalculator.DeviceChanges changes =
			DeviceAuthorityChangesCalculator.calculateChanges(oldDeviceIds, entity.getCheckedlimits());

		// 4. 更新权限基本信息
		this.updateById(entity);

		// 5. 先更新设备关联关系，确保后续权限任务基于最新的设备关联
		updateDeviceRelationsBatch(entity.getId(), parkIds, Arrays.asList(entity.getCheckedlimits()));
		log.info("设备关联关系更新完成，权限ID={}", entity.getId());

		// 6. 如果有设备变更，处理权限任务（基于最新的设备关联关系）
		if (changes.hasChanges()) {
			// 获取实际关联用户数用于精确的任务数量预估
			int actualUserCount = smtStaffDeviceAuthService.count(
				new LambdaQueryWrapper<SmtStaffDeviceAuth>().eq(SmtStaffDeviceAuth::getAuthId, entity.getId())
			);
			// 估算任务数量用于性能监控
			int estimatedTasks = DeviceAuthorityChangesCalculator.estimateTaskCount(changes, actualUserCount);
			log.info("设备权限变更详情: {}, 实际关联用户: {}人, 预估任务数量: {}",
					changes.getChangesSummary(), actualUserCount, estimatedTasks);

			// 根据设备类型处理权限更新
			if (entity.getType().equals(DeviceTypeEnum.DEVICE_TYPE_1.getCode())) {
				// 人脸设备 - 使用优化版本
				updateStaffFaceAuthOptimized(entity.getId(), changes.getDevicesToRemove(), changes.getDevicesToAdd());
			} else if (entity.getType().equals(DeviceTypeEnum.DEVICE_TYPE_3.getCode())) {
				// 车辆设备 - 使用优化版本
				updateVehicleAuthOptimized(entity.getId(), changes.getDevicesToRemove(), changes.getDevicesToAdd());
			}
		} else {
			log.info("设备权限无变更，跳过权限任务处理");
		}

		long endTime = System.currentTimeMillis();
		log.info("设备权限更新完成: 权限ID={}, 总耗时={}ms", entity.getId(), endTime - startTime);

		return true;
	}

	/**
	 * 更新员工人脸权限 - 性能优化版本
	 * 优化点：
	 * 1. 一次性查询所有相关员工权限，避免分页查询
	 * 2. 批量查询员工信息，减少数据库交互
	 * 3. 使用批量任务创建工具，提升插入效率
	 * 4. 添加详细的性能监控日志
	 *
	 * @param authId 权限ID
	 * @param delAuthList 需要删除权限的设备列表
	 * @param addAuthList 需要新增权限的设备列表
	 */
	private void updateStaffFaceAuthOptimized(Integer authId, List<String> delAuthList, List<String> addAuthList) {
		long startTime = System.currentTimeMillis();
		log.info("开始更新员工人脸权限: 权限ID={}, 删除设备{}个, 新增设备{}个",
				authId, delAuthList.size(), addAuthList.size());

		// 如果没有变更，直接返回
		if (delAuthList.isEmpty() && addAuthList.isEmpty()) {
			log.info("无设备变更，跳过员工人脸权限更新");
			return;
		}

		// 1. 一次性查询所有相关员工权限
		List<SmtStaffDeviceAuth> staffDeviceAuths = smtStaffDeviceAuthService.list(
			new LambdaQueryWrapper<SmtStaffDeviceAuth>().eq(SmtStaffDeviceAuth::getAuthId, authId)
		);

		if (staffDeviceAuths.isEmpty()) {
			log.info("权限ID={}无关联员工，跳过权限更新", authId);
			return;
		}

		log.info("查询到{}个员工权限关联记录", staffDeviceAuths.size());

		// 2. 批量查询有效员工信息（只过滤离职员工，保持与原业务逻辑一致）
		List<Long> staffIds = staffDeviceAuths.stream()
			.map(SmtStaffDeviceAuth::getStaffId)
			.collect(Collectors.toList());

		// 修复：使用分批查询避免Oracle IN子句1000个参数限制
		List<SmtStaff> validStaffs = new ArrayList<>();

		// 分批处理staffIds，避免Oracle IN子句限制
		int batchSize = 500; // Oracle安全批次大小
		for (int i = 0; i < staffIds.size(); i += batchSize) {
			int endIndex = Math.min(i + batchSize, staffIds.size());
			List<Long> batchStaffIds = staffIds.subList(i, endIndex);

			log.debug("Oracle分批查询员工信息: 第{}批, 处理{}到{}, 共{}个ID",
					(i / batchSize + 1), i + 1, endIndex, batchStaffIds.size());

			try {
				// 【业务逻辑修复】先查询所有员工，只过滤离职员工，保持与原业务逻辑一致
				List<SmtStaff> allBatchStaffs = smtStaffService.list(
					Wrappers.<SmtStaff>lambdaQuery()
						.in(SmtStaff::getId, batchStaffIds)
				);

				// 过滤离职员工和无人脸图片的员工
				List<SmtStaff> filteredBatchStaffs = allBatchStaffs.stream()
					.filter(staff -> {
						// 过滤离职员工
						if (StaffStatusEnum.STAFF_STATUS_QUIT.getCode().equals(staff.getStatus())) {
							log.debug("过滤离职员工: {}", staff.getName());
							return false;
						}
						// 过滤无人脸图片的员工
						if (StringUtils.isEmpty(staff.getFacePicId())) {
							log.debug("过滤无人脸图片员工: {}", staff.getName());
							return false;
						}
						return true;
					})
					.collect(Collectors.toList());

				validStaffs.addAll(filteredBatchStaffs);

				log.debug("第{}批查询结果: 原始{}人, 过滤离职后{}人",
						(i / batchSize + 1), allBatchStaffs.size(), filteredBatchStaffs.size());

			} catch (Exception e) {
				log.error("Oracle分批查询第{}批员工信息失败: {}", (i / batchSize + 1), e.getMessage(), e);
				// 继续处理下一批，避免单批错误影响整体流程
				continue;
			}
		}

		log.info("过滤后有效员工{}人（已过滤离职员工和无人脸图片员工）", validStaffs.size());

		// 3. 批量创建权限任务（仅为有人脸图片的在职员工创建任务）
		if (!validStaffs.isEmpty()) {
			try {
				int createdTaskCount = smtBatchDeviceTaskService.createStaffFaceAuthTasks(
					validStaffs, delAuthList, addAuthList
				);
				log.info("批量创建权限任务完成: 成功创建{}个任务", createdTaskCount);
			} catch (Exception e) {
				log.error("批量创建权限任务异常，回退到逐个创建模式", e);
				// 回退到传统方式，确保兼容性
				createTasksIndividually(validStaffs, delAuthList, addAuthList);
			}
		}

		long endTime = System.currentTimeMillis();
		log.info("员工人脸权限更新完成: 权限ID={}, 总耗时={}ms", authId, endTime - startTime);
	}

	/**
	 * 回退模式：逐个创建任务（仅处理有人脸图片的在职员工）
	 */
	private void createTasksIndividually(List<SmtStaff> staffList, List<String> delAuthList, List<String> addAuthList) {
		log.info("使用回退模式逐个创建任务");
		int taskCount = 0;

		for (SmtStaff staff : staffList) {
			try {
				String gen = staff.getBadge() + SymbolConstants.MINUS + staff.getName();

				// 生成删除任务
				if (!delAuthList.isEmpty()) {
					smtDeviceTaskService.addDeviceDelTaskImmed(delAuthList, staff.getId().toString(), gen,
							DeviceTaskConstants.CARD_STAFF_IMPORT, DeviceTaskActionEnum.DELAY_DEL.getCode(),
							SmtVisitorEnum.CAR_CARD_TYPE_1, DeviceTaskConstants.CARD, staff.getFacePicId());
					taskCount += delAuthList.size();
				}

				// 生成新增任务
				if (!addAuthList.isEmpty()) {
					smtDeviceTaskService.addDeviceTask(addAuthList, staff.getId().toString(), gen,
							DeviceTaskConstants.CARD_STAFF_IMPORT, DeviceTaskActionEnum.DELAY_DOWN.getCode(),
							SmtVisitorEnum.CAR_CARD_TYPE_1, DeviceTaskConstants.CARD, staff.getFacePicId(), null);
					taskCount += addAuthList.size();
				}
			} catch (Exception e) {
				log.error("为员工{}创建任务失败", staff.getName(), e);
			}
		}

		log.info("回退模式完成: 共创建{}个任务", taskCount);
	}

	/**
	 * 更新车辆权限 - 性能优化版本
	 * 优化点：
	 * 1. 一次性查询所有相关车辆权限，避免分页查询
	 * 2. 使用批量任务创建工具，提升插入效率
	 * 3. 添加详细的性能监控日志
	 *
	 * @param authId 权限ID
	 * @param delAuthList 需要删除权限的设备列表
	 * @param addAuthList 需要新增权限的设备列表
	 */
	private void updateVehicleAuthOptimized(Integer authId, List<String> delAuthList, List<String> addAuthList) {
		long startTime = System.currentTimeMillis();
		log.info("开始更新车辆权限: 权限ID={}, 删除设备{}个, 新增设备{}个",
				authId, delAuthList.size(), addAuthList.size());

		// 如果没有变更，直接返回
		if (delAuthList.isEmpty() && addAuthList.isEmpty()) {
			log.info("无设备变更，跳过车辆权限更新");
			return;
		}

		// 1. 一次性查询所有相关车辆权限（使用现有方法的优化版本）
		List<VehicleAuthDTO> vehicleAuthList = getAllVehicleAuthByAuthId(authId);

		if (vehicleAuthList.isEmpty()) {
			log.info("权限ID={}无关联车辆，跳过权限更新", authId);
			return;
		}

		log.info("查询到{}个车辆权限关联记录", vehicleAuthList.size());

		// 2. 转换为批量任务创建工具需要的格式
		List<SmtBatchDeviceTaskService.VehicleInfo> vehicleInfoList = vehicleAuthList.stream()
			.map(v -> new SmtBatchDeviceTaskService.VehicleInfo(v.getVid().toString(), v.getVehiclePlate()))
			.collect(Collectors.toList());

		// 3. 批量创建权限任务（统一使用saveTask入口）
		int createdTaskCount = smtBatchDeviceTaskService.createVehicleAuthTasks(
			vehicleInfoList, delAuthList, addAuthList
		);

		log.info("批量创建车辆权限任务完成，共创建{}个任务", createdTaskCount);

		long endTime = System.currentTimeMillis();
		log.info("车辆权限更新完成: 权限ID={}, 总耗时={}ms", authId, endTime - startTime);
	}

	/**
	 * 更新车辆权限 - 原版本（保留作为备用）
	 * @param authId		权限Id
	 * @param delAuthList	需要删除权限的设备
	 * @param addAuthList	需要下发权限的设备
	 */
	@Deprecated
	private void updateVehicleAuth(Integer authId,List<String> delAuthList,List<String> addAuthList){
		//分页查询所有关联该权限的车辆权限数据
		Page vehiclePage = new Page(1,100);
		boolean isNext = false;
		do{
			//查询一页数据
			IPage<VehicleAuthDTO> staffVehiclePage = smtVehicleMapper.getVehicleAuthAll(vehiclePage, authId);

			List<VehicleAuthDTO> VehicleAuthList = staffVehiclePage.getRecords();

			if(CollectionUtil.isNotEmpty(VehicleAuthList)) {
				//修改车辆权限
				VehicleAuthList.forEach(item -> {
					//生成旧权限删除的任务
					smtDeviceTaskService.addDeviceDelTaskImmed(delAuthList,item.getVid().toString(),item.getVehiclePlate(),DeviceTaskConstants.CAR_STAFF, DeviceTaskActionEnum.DELAY_DEL.getCode(), SmtVisitorEnum.CAR_CARD_TYPE_1,DeviceTaskConstants.CAR,null);

					//生成下发新权限的任务
					smtDeviceTaskService.addDeviceTask(addAuthList,item.getVid().toString(),item.getVehiclePlate(),DeviceTaskConstants.CAR_STAFF,
							DeviceTaskActionEnum.DELAY_DOWN.getCode(),SmtVisitorEnum.CAR_CARD_TYPE_1,DeviceTaskConstants.CAR,null, null);
				});
			}
			if(vehiclePage.hasNext()){
				//下一页
				vehiclePage.setCurrent(vehiclePage.getCurrent() + 1);
				isNext = true;
			} else {
				isNext = false;
			}
		} while (isNext);
	}

	/**
	 * 删除设备权限信息
	 *
	 * @param id 设备权限ID
	 * @return 返回结果
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public Result deleteDeviceAuthority(Integer id, List<Integer> parkIds) {
		if(employeeAuthOperationAdapter!=null)employeeAuthOperationAdapter.guardAuthorityDeletion(id);
		if (DeviceAuthorityEnum.existAuthority(id)) {
			return new Result<>(false, "该权限策略为系统默认权限不可删除");
		}
		//List<SmtDeviceAuthorityRelation> list = smtDeviceAuthorityRelationService.list(Wrappers.<SmtDeviceAuthorityRelation>query().lambda().eq(SmtDeviceAuthorityRelation::getAuthorityId,id).notIn(CollUtil.isNotEmpty(parkIds),SmtDeviceAuthorityRelation::getParkId,parkIds));
		int count = smtBusinessDeviceAuthService.count(Wrappers.<SmtBusinessDeviceAuth>lambdaQuery()
				.eq(Objects.nonNull(id), SmtBusinessDeviceAuth::getAuthId, id));
		if (count > 0) {
			return new Result<>(false, "该权限策略已被使用，删除请先在区域管理/配置/通行权限界面取消默认权限");
		}
		count = smtStaffDeviceAuthService.count(Wrappers.<SmtStaffDeviceAuth>lambdaQuery().eq(SmtStaffDeviceAuth::getAuthId, id));
		if (count > 0) {
			return new Result<>(false, "该权限策略已被使用，请先在区域管理/配置/通行权限界面删除关联人员");
		}
		count = smtVehicleApplyService.count(Wrappers.<SmtVehicleApply>lambdaQuery().eq(SmtVehicleApply::getAuthorityId, id));
		if (count > 0) {
			return new Result<>(false, "该权限策略已被使用，请先在区域管理/配置/通行权限界面删除关联车辆");
		}
		smtDeviceAuthorityRelationService.remove(Wrappers.<SmtDeviceAuthorityRelation>query().lambda().eq(SmtDeviceAuthorityRelation::getAuthorityId, id));
		return new Result<>(this.removeById(id));
	}

	/**
	 * 获取权限策略详情
	 *
	 * @param id 权限策略ID
	 * @return 返回结果
	 */
	@Override
	public SmtDeviceAuthorityDTO getDeviceAuthorityById(Integer id, List<Integer> parkIds) {
		if(employeeAuthOperationAdapter!=null)employeeAuthOperationAdapter.checkAuthority(id);
		SmtDeviceAuthority smtDeviceAuthority = this.getById(id);
		SmtDeviceAuthorityDTO smtDeviceAuthorityDTO = new SmtDeviceAuthorityDTO();
		BeanUtils.copyProperties(smtDeviceAuthority, smtDeviceAuthorityDTO);
		List<String> list = smtDeviceAuthorityRelationService.getDeviceIds(id, parkIds);
		String[] checkedlimits = new String[list.size()];
		smtDeviceAuthorityDTO.setCheckedlimits(list.toArray(checkedlimits));
		return smtDeviceAuthorityDTO;
	}

	@Override
	public List<DeviceTree> getTree(Integer type, List<Integer> parkIds) {
		List<DeviceTree> list = smtDeviceAuthorityRelationService.getPark(parkIds);
		List<Integer> types = new ArrayList<>(1);
		types.add(type);
		for (DeviceTree deviceTree : list) {
			deviceTree.setChildren(this.children(Integer.parseInt(deviceTree.getId()), 0, types));
		}
		return list;
	}

	@Override
	public List<DeviceTree> getTrees(List<Integer> parkIds, Integer areaType) {
		List<DeviceTree> list = smtDeviceAuthorityRelationService.getPark(parkIds);
		List<Integer> types = new ArrayList<>(2);
		types.add(DeviceTypeEnum.DEVICE_TYPE_1.getCode());
		types.add(DeviceTypeEnum.DEVICE_TYPE_2.getCode());
		if (areaType == null) {
			for (DeviceTree deviceTree : list) {
				deviceTree.setChildren(this.children(Integer.parseInt(deviceTree.getId()), 0, types));
			}
		} else {
			for (DeviceTree deviceTree : list) {
				deviceTree.setChildren(this.children(Integer.parseInt(deviceTree.getId()), 0, types, areaType));
			}
		}
		return list;
	}

	@Override
	public List<SmtDeviceAuthority> list(Integer type, Integer parkId, String name) {
		return this.baseMapper.selectList(Wrappers.<SmtDeviceAuthority>query().lambda().eq(SmtDeviceAuthority::getType, type)
				.like(StringUtils.isNotEmpty(name), SmtDeviceAuthority::getAuthorityName, name)
				.eq(SmtDeviceAuthority::getParkId, parkId));
	}

	@Override
	public List<SmtDeviceAuthority> listAll( Integer parkId) {
		return this.baseMapper.selectList(Wrappers.<SmtDeviceAuthority>query().lambda()
				.eq(SmtDeviceAuthority::getAreaType, OneOrZeroEnum.ZERO.getCode())
				.eq(SmtDeviceAuthority::getParkId, parkId));
	}

	private List<DeviceTree> children(Integer parkId, Integer pId, List<Integer> types) {
		List<DeviceTree> list = smtDeviceAuthorityRelationService.getArea(parkId, pId);
		for (DeviceTree deviceTree : list) {
			deviceTree.setChildren(this.device(smtDeviceAuthorityRelationService.getArea(null, Integer.parseInt(deviceTree.getId())), types));
		}
		return list;
	}

	private List<DeviceTree> children(Integer parkId, Integer pId, List<Integer> types, Integer areaType) {
		List<DeviceTree> list = smtDeviceAuthorityRelationService.getArea(parkId, pId);
		for (DeviceTree deviceTree : list) {
			deviceTree.setChildren(this.device(smtDeviceAuthorityRelationService.getArea(null, Integer.parseInt(deviceTree.getId())), types, areaType));
		}
		return list;
	}

	private List<DeviceTree> device(List<DeviceTree> list, List<Integer> types) {
		for (DeviceTree deviceTree : list) {
			deviceTree.setChildren(smtDeviceAuthorityRelationService.getDevice(deviceTree.getId(), types));
		}
		return list;
	}

	private List<DeviceTree> device(List<DeviceTree> list, List<Integer> types, Integer areaType) {
		for (DeviceTree deviceTree : list) {
			List<DeviceTree> deviceTreeList = smtDeviceAuthorityRelationService.getDevice(deviceTree.getId(), types);
			deviceTree.setChildren(deviceTreeList.stream().filter(item -> !checkIsUsed(areaType, item.getId())).collect(Collectors.toList()));
		}
		return list;
	}

	@Override
	public IPage<DeviceAuthorityVO> page(Page page, SmtDeviceAuthority smtDeviceAuthority) {
		// TODO Auto-generated method stub
		List<Integer> parkIdList = SecurityUtils.getUser().getParkIdList();
		IPage<DeviceAuthorityVO> list = this.baseMapper.getDeviceAuthPage(page, smtDeviceAuthority, parkIdList);
		return list;
	}

	@Override
	public IPage<AuthDetailRespDTO> getAuthDetailPage(Page page, AuthDetailQueryDTO queryDTO) {
        if(employeeAuthOperationAdapter!=null)employeeAuthOperationAdapter.checkAuthority(queryDTO.getAuthId());
		List<String> badgeList = new ArrayList<>();
		if(StringUtils.isNotEmpty(queryDTO.getBadges())){
			badgeList.addAll(ToolUtils.splitBlankString(queryDTO.getBadges()));
		}
		if(CollectionUtil.isNotEmpty(badgeList) && badgeList.size() > 200){
			throw new SmartException("最多查询两百条记录");
		}
		if (DeviceAuthTypeEnum.PERSON.getCode().equals(queryDTO.getType())) {
			return this.baseMapper.getPersonAuthDetailPage(page, queryDTO,badgeList);
		} else if (DeviceAuthTypeEnum.VEHICLE.getCode().equals(queryDTO.getType())) {
			return this.baseMapper.getVehicleAuthDetailPage(page, queryDTO);
		}
		return new Page<>();
	}

    @Override
    public AuthOperationIntakeReceipt personRelationDeleteIntake(DeviceAuthRelationDelReqDTO request, String requestKey, Integer actorId, List<Integer> allowedParks) {
        if(request==null)throw new IllegalArgumentException("请求不能为空");
        AuthOperationIntakeCommand command=AuthOperationIntakeCommand.builder().requestKey(requestKey).requestKind("REMOVE_ROWS")
          .authId(request.getAuthId()).authorityType(request.getType()).rowIds(request.getDelIds()==null?Collections.emptyList():request.getDelIds()).build();
        return employeeAuthIntakeService.submit(command,actorId,allowedParks==null?null:new HashSet<>(allowedParks),key->{
            requireReceiptAuthority(request.getAuthId(),allowedParks);
            if(employeeAuthOperationAdapter==null)throw new EmployeeAuthIntakeService.IntakeException("KEYED_UNSUPPORTED");
            return employeeAuthOperationAdapter.removeRowsOperation(command.getRowIds(),command.getAuthId(),key);
        });
    }
    @Override
    public AuthOperationIntakeReceipt personRelationClearIntake(Integer id, String requestKey, Integer actorId, List<Integer> allowedParks) {
        AuthOperationIntakeCommand command=AuthOperationIntakeCommand.builder().requestKey(requestKey).requestKind("CLEAR_AUTHORITY")
          .authId(id).authorityType(DeviceAuthTypeEnum.PERSON.getCode()).build();
        return employeeAuthIntakeService.submit(command,actorId,allowedParks==null?null:new HashSet<>(allowedParks),key->{
            requireReceiptAuthority(id,allowedParks);
            if(employeeAuthOperationAdapter==null)throw new EmployeeAuthIntakeService.IntakeException("KEYED_UNSUPPORTED");
            return employeeAuthOperationAdapter.removeAuthorityOperation(id,key);
        });
    }
    @Override
    public AuthOperationIntakeCapability personIntakeCapability(Integer id, List<Integer> allowedParks) {
        requireReceiptAuthority(id,allowedParks);
        return new AuthOperationIntakeCapability(1,employeeAuthOperationAdapter!=null && employeeAuthOperationAdapter.enabledAuthority(id));
    }

    @Override
    public AuthOperationReceipt personRelationDeleteReceipt(DeviceAuthRelationDelReqDTO request, List<Integer> allowedParks) {
        if (request == null || !DeviceAuthTypeEnum.PERSON.getCode().equals(request.getType()))
            throw new IllegalArgumentException("此入口仅支持人员权限");
        requireReceiptAuthority(request.getAuthId(), allowedParks);
        if (request.getDelIds() == null || request.getDelIds().isEmpty()
                || request.getDelIds().stream().anyMatch(id -> id == null || id <= 0))
            throw new IllegalArgumentException("删除列表不能为空或包含无效ID");
        List<Integer> ids = new ArrayList<>(new LinkedHashSet<>(request.getDelIds()));
        // 即使灰度关闭也必须校验所有行归属，不能让旧路径删除别的权限组。
        for (int start = 0; start < ids.size(); start += 200) {
            List<Integer> part = ids.subList(start, Math.min(start + 200, ids.size()));
            Collection<SmtStaffDeviceAuth> selected = smtStaffDeviceAuthService.listByIds(part);
            Set<Integer> found = selected.stream().map(SmtStaffDeviceAuth::getId).collect(Collectors.toSet());
            if (!found.equals(new HashSet<>(part))) throw new IllegalArgumentException("所选来源不存在，请刷新后核验");
            if (selected.stream().anyMatch(row -> !request.getAuthId().equals(row.getAuthId())))
                throw new SecurityException("来源不属于指定权限组");
        }
        String key = employeeAuthOperationAdapter == null ? null : employeeAuthOperationAdapter.removeRowsOperation(ids, request.getAuthId());
        if (key != null) return AuthOperationReceipt.reliable(key);
        DeviceAuthRelationDelReqDTO legacy = new DeviceAuthRelationDelReqDTO();
        legacy.setAuthId(request.getAuthId()); legacy.setType(DeviceAuthTypeEnum.PERSON.getCode()); legacy.setDelIds(ids);
        return AuthOperationReceipt.legacy(legacyDeviceAuthRelationDel(legacy));
    }

    @Override
    public AuthOperationReceipt personRelationClearReceipt(Integer id, List<Integer> allowedParks) {
        requireReceiptAuthority(id, allowedParks);
        String key = employeeAuthOperationAdapter == null ? null : employeeAuthOperationAdapter.removeAuthorityOperation(id);
        if (key != null) return AuthOperationReceipt.reliable(key);
        return AuthOperationReceipt.legacy(legacyDeviceAuthRelationClear(id));
    }

    /** 真实组类型和园区不能由请求参数替换，也不依赖灰度开关。 */
    private void requireReceiptAuthority(Integer id, List<Integer> allowedParks) {
        if (id == null || id <= 0) throw new IllegalArgumentException("权限组ID无效");
        if (allowedParks == null || allowedParks.isEmpty()) throw new SecurityException("缺少明确的允许园区范围");
        SmtDeviceAuthority authority = getById(id);
        if (authority == null) throw new IllegalArgumentException("权限组不存在");
        if (authority.getParkId() == null || !allowedParks.contains(authority.getParkId())) throw new SecurityException("无权限组所属园区权限");
        if (!DeviceAuthTypeEnum.PERSON.getCode().equals(authority.getType())) throw new IllegalArgumentException("此入口仅支持人员权限组");
    }

	@Override
	public Boolean deviceAuthRelationDel(DeviceAuthRelationDelReqDTO reqDTO) {
		if(employeeAuthOperationAdapter!=null && DeviceAuthTypeEnum.PERSON.getCode().equals(reqDTO.getType())){
            Boolean accepted=employeeAuthOperationAdapter.removeRows(reqDTO.getDelIds(),reqDTO.getAuthId());if(accepted!=null)return accepted;
        }
        return legacyDeviceAuthRelationDel(reqDTO);
    }

    /** 已确定走旧流程后不再重新选择灰度入口。 */
    private Boolean legacyDeviceAuthRelationDel(DeviceAuthRelationDelReqDTO reqDTO) {
		List<Integer> authIds = new ArrayList<>(1);
		authIds.add(reqDTO.getAuthId());
		// 查询该授权关联的设备信息
		List<SmtDeviceAuthorityRelation> relationList = smtDeviceAuthorityRelationService.getRelationByAuthId(authIds);

		if (CollUtil.isEmpty(relationList)) {
			if (DeviceAuthTypeEnum.PERSON.getCode().equals(reqDTO.getType())) {
				return smtStaffDeviceAuthService.removeByIds(reqDTO.getDelIds());
			} else {
				return smtVehicleApplyService.removeByIds(reqDTO.getDelIds());
			}
		}

		List<String> deviceIds = relationList.stream().map(SmtDeviceAuthorityRelation::getDeviceId).collect(Collectors.toList());
		if (DeviceAuthTypeEnum.PERSON.getCode().equals(reqDTO.getType())) {
			// 批量查询待删除的权限记录，避免循环内逐条 getById（N+1）；
			// 注意：若某个 delId 已不存在（例如并发场景下被其他请求提前删除），listByIds 会直接跳过它，
			// 不会像原来 getById+空指针 那样让整批操作崩溃，这里记录日志便于排查“为什么这条记录没有被处理”
			List<SmtStaffDeviceAuth> staffAuthList = new ArrayList<>(smtStaffDeviceAuthService.listByIds(reqDTO.getDelIds()));
			if (staffAuthList.size() != reqDTO.getDelIds().size()) {
				log.warn("批量删除人员权限时发现{}条记录已不存在，权限ID={}，请求删除ID={}，实际查到={}",
						reqDTO.getDelIds().size() - staffAuthList.size(), reqDTO.getAuthId(), reqDTO.getDelIds(),
						staffAuthList.stream().map(SmtStaffDeviceAuth::getId).collect(Collectors.toList()));
			}
			// 批量计算每个员工在其他权限下仍需保留的设备，避免循环内逐条查询（N+1）
			Map<Long, List<String>> otherDeviceIdsByStaffId = buildOtherDeviceIdsByStaffId(staffAuthList, reqDTO.getAuthId());
			for (SmtStaffDeviceAuth staffAuth : staffAuthList) {
				List<String> removableDeviceIds = new ArrayList<>(deviceIds);
				List<String> otherDeviceIds = otherDeviceIdsByStaffId.get(staffAuth.getStaffId());
				if (CollUtil.isNotEmpty(otherDeviceIds)) {
					log.info("删除人员{}保留其他权限关联设备：{}", staffAuth.getId(), otherDeviceIds);
					removableDeviceIds.removeAll(otherDeviceIds);
				}
				smtStaffDeviceAuthService.removeAuthToDevice(staffAuth, removableDeviceIds);
			}
		} else {
			// 批量查询待删除的权限记录，避免循环内逐条 getById（N+1）；
			// 注意：若某个 delId 已不存在（例如并发场景下被其他请求提前删除），listByIds 会直接跳过它，
			// 不会像原来 getById+空指针 那样让整批操作崩溃，这里记录日志便于排查“为什么这条记录没有被处理”
			List<SmtVehicleApply> vehicleApplyList = new ArrayList<>(smtVehicleApplyService.listByIds(reqDTO.getDelIds()));
			if (vehicleApplyList.size() != reqDTO.getDelIds().size()) {
				log.warn("批量删除车辆权限时发现{}条记录已不存在，权限ID={}，请求删除ID={}，实际查到={}",
						reqDTO.getDelIds().size() - vehicleApplyList.size(), reqDTO.getAuthId(), reqDTO.getDelIds(),
						vehicleApplyList.stream().map(SmtVehicleApply::getId).collect(Collectors.toList()));
			}
			// 批量计算每辆车在其他权限下仍需保留的设备，避免循环内逐条查询（N+1）
			Map<Long, List<String>> otherDeviceIdsByVehicleId = buildOtherDeviceIdsByVehicleId(vehicleApplyList, reqDTO.getAuthId());
			for (SmtVehicleApply vehicleApply : vehicleApplyList) {
				List<String> removableDeviceIds = new ArrayList<>(deviceIds);
				List<String> otherDeviceIds = otherDeviceIdsByVehicleId.get(vehicleApply.getVehicleId());
				if (CollUtil.isNotEmpty(otherDeviceIds)) {
					log.info("删除车辆{}保留其他权限关联设备：{}", vehicleApply.getId(), otherDeviceIds);
					removableDeviceIds.removeAll(otherDeviceIds);
				}
				smtVehicleApplyService.removeAuthToDevice(vehicleApply, removableDeviceIds);
			}
		}
		return true;
	}

	@Override
	public Boolean deviceAuthRelationClear(Integer id) {
		if(employeeAuthOperationAdapter!=null){Boolean accepted=employeeAuthOperationAdapter.removeAuthority(id);if(accepted!=null)return accepted;}
        return legacyDeviceAuthRelationClear(id);
    }

    /** 保留旧 Boolean 流程，仅由已确定模式的调用者进入。 */
    private Boolean legacyDeviceAuthRelationClear(Integer id) {
		SmtDeviceAuthority deviceAuthority = getById(id);
		List<Integer> authRelIds = new ArrayList<>(1);
		authRelIds.add(id);
		// 查询该授权关联的设备信息
		List<SmtDeviceAuthorityRelation> relationList = smtDeviceAuthorityRelationService.getRelationByAuthId(authRelIds);

		if (CollUtil.isEmpty(relationList)) {
			if (DeviceAuthTypeEnum.PERSON.getCode().equals(deviceAuthority.getType())) {
				return smtStaffDeviceAuthService.removeByAuthId(id);
			} else {
				return smtVehicleApplyService.removeByAuthId(id);
			}
		}

		List<String> deviceIds = relationList.stream().map(SmtDeviceAuthorityRelation::getDeviceId).collect(Collectors.toList());

		if (DeviceAuthTypeEnum.PERSON.getCode().equals(deviceAuthority.getType())) {
			List<SmtStaffDeviceAuth> staffAuthList = smtStaffDeviceAuthService.list(Wrappers.<SmtStaffDeviceAuth>lambdaQuery().eq(SmtStaffDeviceAuth::getAuthId, id));
			// 批量计算每个员工在其他权限下仍需保留的设备，避免循环内逐条查询（N+1）
			Map<Long, List<String>> otherDeviceIdsByStaffId = buildOtherDeviceIdsByStaffId(staffAuthList, id);
			for (SmtStaffDeviceAuth auth : staffAuthList) {
				List<String> removableDeviceIds = new ArrayList<>(deviceIds);
				List<String> otherDeviceIds = otherDeviceIdsByStaffId.get(auth.getStaffId());
				if (CollUtil.isNotEmpty(otherDeviceIds)) {
					log.info("删除人员{}保留其他权限关联设备：{}", auth.getId(), otherDeviceIds);
					removableDeviceIds.removeAll(otherDeviceIds);
				}
				smtStaffDeviceAuthService.removeAuthToDevice(auth, removableDeviceIds);
			}
		} else {
			List<SmtVehicleApply> vehicleApplyList = smtVehicleApplyService.list(Wrappers.<SmtVehicleApply>lambdaQuery().eq(SmtVehicleApply::getAuthorityId, id));
			// 批量计算每辆车在其他权限下仍需保留的设备，避免循环内逐条查询（N+1）
			Map<Long, List<String>> otherDeviceIdsByVehicleId = buildOtherDeviceIdsByVehicleId(vehicleApplyList, id);
			for (SmtVehicleApply delAuth : vehicleApplyList) {
				List<String> removableDeviceIds = new ArrayList<>(deviceIds);
				List<String> otherDeviceIds = otherDeviceIdsByVehicleId.get(delAuth.getVehicleId());
				if (CollUtil.isNotEmpty(otherDeviceIds)) {
					log.info("删除车辆{}保留其他权限关联设备：{}", delAuth.getId(), otherDeviceIds);
					removableDeviceIds.removeAll(otherDeviceIds);
				}
				smtVehicleApplyService.removeAuthToDevice(delAuth, removableDeviceIds);
			}
		}
		return true;
	}

	/**
	 * 批量计算人员在【其他权限】下仍保留访问权限的设备，避免对每条待删除/清空记录单独查询。
	 * 原实现是在循环里对每个人员各查一次"该员工的其他权限列表"、再查一次"这些权限关联的设备"，
	 * 选中/清空的记录数越多，数据库往返次数越多（经典 N+1）。这里改为按去重后的 staffId
	 * 批量查一次"其他权限记录"，再按去重后的 authId 批量查一次"关联设备"，
	 * 在内存里用 Map 归并，数据库往返次数固定，与记录条数无关。
	 *
	 * 返回值里同一员工的设备ID列表可能含重复（同一员工的多个其他权限覆盖了同一台设备），
	 * 这里不做去重——调用方只用它做 removeAll 剔除，重复元素不影响结果。
	 *
	 * @param staffAuthList 本次待删除/清空的人员权限记录
	 * @param excludeAuthId 当前正在删除/清空的权限ID（查询"其他权限"时需要排除掉它）
	 * @return staffId -> 该员工在其他权限下仍保留访问权限的设备ID列表
	 */
	private Map<Long, List<String>> buildOtherDeviceIdsByStaffId(List<SmtStaffDeviceAuth> staffAuthList, Integer excludeAuthId) {
		if (CollUtil.isEmpty(staffAuthList)) {
			return Collections.emptyMap();
		}
		List<Long> staffIds = staffAuthList.stream().map(SmtStaffDeviceAuth::getStaffId).distinct().collect(Collectors.toList());
		// 生产实证单个权限组关联人员会超过 1000，staffIds 的 IN 查询必须分批，
		// 否则 ORA-01795 直接让整个清空/删除事务回滚（staffIds 已去重，分批不会重复查询）
		List<SmtStaffDeviceAuth> otherAuthList = OracleInBatchUtils.listInBatches(staffIds,
				batchStaffIds -> smtStaffDeviceAuthService.list(Wrappers.<SmtStaffDeviceAuth>lambdaQuery()
						.in(SmtStaffDeviceAuth::getStaffId, batchStaffIds)
						.ne(SmtStaffDeviceAuth::getAuthId, excludeAuthId)));
		if (CollUtil.isEmpty(otherAuthList)) {
			return Collections.emptyMap();
		}
		List<Integer> otherAuthIds = otherAuthList.stream().map(SmtStaffDeviceAuth::getAuthId).distinct().collect(Collectors.toList());
		Map<Integer, List<String>> deviceIdsByAuthId = smtDeviceAuthorityRelationService.getRelationByAuthId(otherAuthIds).stream()
				.collect(Collectors.groupingBy(SmtDeviceAuthorityRelation::getAuthorityId,
						Collectors.mapping(SmtDeviceAuthorityRelation::getDeviceId, Collectors.toList())));
		Map<Long, List<String>> otherDeviceIdsByStaffId = new HashMap<>();
		for (SmtStaffDeviceAuth otherAuth : otherAuthList) {
			otherDeviceIdsByStaffId.computeIfAbsent(otherAuth.getStaffId(), k -> new ArrayList<>())
					.addAll(deviceIdsByAuthId.getOrDefault(otherAuth.getAuthId(), Collections.emptyList()));
		}
		return otherDeviceIdsByStaffId;
	}

	/**
	 * 批量计算车辆在【其他权限】下仍保留访问权限的设备，思路同 {@link #buildOtherDeviceIdsByStaffId}。
	 *
	 * @param vehicleApplyList 本次待删除/清空的车辆权限记录
	 * @param excludeAuthId    当前正在删除/清空的权限ID（查询"其他权限"时需要排除掉它）
	 * @return vehicleId -> 该车辆在其他权限下仍保留访问权限的设备ID列表
	 */
	private Map<Long, List<String>> buildOtherDeviceIdsByVehicleId(List<SmtVehicleApply> vehicleApplyList, Integer excludeAuthId) {
		if (CollUtil.isEmpty(vehicleApplyList)) {
			return Collections.emptyMap();
		}
		List<Long> vehicleIds = vehicleApplyList.stream().map(SmtVehicleApply::getVehicleId).distinct().collect(Collectors.toList());
		// 与员工侧同理：车辆数同样无上界，vehicleIds 的 IN 查询必须分批（已去重）
		List<SmtVehicleApply> otherAuthList = OracleInBatchUtils.listInBatches(vehicleIds,
				batchVehicleIds -> smtVehicleApplyService.list(Wrappers.<SmtVehicleApply>lambdaQuery()
						.in(SmtVehicleApply::getVehicleId, batchVehicleIds)
						.ne(SmtVehicleApply::getAuthorityId, excludeAuthId)));
		if (CollUtil.isEmpty(otherAuthList)) {
			return Collections.emptyMap();
		}
		List<Integer> otherAuthIds = otherAuthList.stream().map(SmtVehicleApply::getAuthorityId).distinct().collect(Collectors.toList());
		Map<Integer, List<String>> deviceIdsByAuthId = smtDeviceAuthorityRelationService.getRelationByAuthId(otherAuthIds).stream()
				.collect(Collectors.groupingBy(SmtDeviceAuthorityRelation::getAuthorityId,
						Collectors.mapping(SmtDeviceAuthorityRelation::getDeviceId, Collectors.toList())));
		Map<Long, List<String>> otherDeviceIdsByVehicleId = new HashMap<>();
		for (SmtVehicleApply otherAuth : otherAuthList) {
			otherDeviceIdsByVehicleId.computeIfAbsent(otherAuth.getVehicleId(), k -> new ArrayList<>())
					.addAll(deviceIdsByAuthId.getOrDefault(otherAuth.getAuthorityId(), Collections.emptyList()));
		}
		return otherDeviceIdsByVehicleId;
	}

	@Override
	public Boolean checkIsUsed(Integer type, String deviceId) {
		return this.baseMapper.countByAreaType(type, deviceId) > 0;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Result<AreaTypeSwitchRespDTO> switchAreaType(AreaTypeSwitchReqDTO reqDTO, List<Integer> parkIds) {
		SmtDeviceAuthority authority = this.getById(reqDTO.getId());
		AreaTypeSwitchRespDTO resp = new AreaTypeSwitchRespDTO();
		if (authority == null || !parkIds.contains(authority.getParkId())) {
			// 注意：不能写成 new Result<>(false, "...")——方法返回类型是 Result<AreaTypeSwitchRespDTO>，
			// 泛型参数已经被返回类型固定了，构造函数第一个参数必须是 AreaTypeSwitchRespDTO 而不是 Boolean，
			// 否则编译期泛型推断会直接报错（规划阶段已经用真实编译踩过这个坑）
			resp.setSuccess(false);
			resp.setConflicts(Collections.emptyList());
			return new Result<>(resp, "权限策略不存在或不在当前用户可操作的园区范围内");
		}

		// 目标性质和当前一致：幂等短路，不查冲突也不写库
		if (Objects.equals(authority.getAreaType(), reqDTO.getAreaType())) {
			resp.setSuccess(true);
			resp.setConflicts(Collections.emptyList());
			return new Result<>(resp);
		}

		List<AreaTypeConflictDeviceVO> conflicts = smtDeviceAuthorityMapper.findAreaTypeConflicts(
				reqDTO.getId(), reqDTO.getAreaType());
		if (CollUtil.isNotEmpty(conflicts)) {
			// 存在跨权限组冲突：直接拒绝，不动 area_type，也不动任何设备/员工/车辆关联数据
			resp.setSuccess(false);
			resp.setConflicts(conflicts);
			return new Result<>(resp);
		}

		// 无冲突：只更新 area_type 这一个字段，不触碰设备关联关系
		SmtDeviceAuthority update = new SmtDeviceAuthority();
		update.setId(reqDTO.getId());
		update.setAreaType(reqDTO.getAreaType());
		this.updateById(update);

		log.info("通关权限性质已切换: 权限ID={}, {} -> {}", reqDTO.getId(), authority.getAreaType(), reqDTO.getAreaType());

		resp.setSuccess(true);
		resp.setConflicts(Collections.emptyList());
		return new Result<>(resp);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public List<String> deviceAuthRelationAdd(DeviceAuthRelationAddReqDTO reqDTO) {
		if(employeeAuthOperationAdapter!=null){List<String> accepted=employeeAuthOperationAdapter.addBadges(reqDTO.getAuthId(),reqDTO.getBadges(),reqDTO.getStartTime(),reqDTO.getEndTime());if(accepted!=null)return accepted;}
		// 在查询或写入前统一校验有效期，批量入口与员工入口保持相同语义。
		PermissionValidityWindow validityWindow = PermissionValidityWindow.resolve(reqDTO.getStartTime(), reqDTO.getEndTime());
		List<SmtStaffDeviceAuth> existAuthList = smtStaffDeviceAuthService.list(Wrappers.<SmtStaffDeviceAuth>lambdaQuery().eq(SmtStaffDeviceAuth::getAuthId, reqDTO.getAuthId()));
		List<Long> existList = existAuthList.stream().map(SmtStaffDeviceAuth::getStaffId).distinct().collect(Collectors.toList());

		List<SmtDeviceAuthorityRelation> authDeviceList = smtDeviceAuthorityRelationService.list(Wrappers.<SmtDeviceAuthorityRelation>lambdaQuery().eq(SmtDeviceAuthorityRelation::getAuthorityId, reqDTO.getAuthId()));
		List<String> newDeviceList = authDeviceList.stream().map(SmtDeviceAuthorityRelation::getDeviceId)
				.filter(StringUtils::isNotEmpty).distinct().collect(Collectors.toList());

		List<String> noExist = new ArrayList<>();
		// badges 来自 HTTP 外部输入，条数无上界，IN 查询必须分批（超 1000 即 ORA-01795 整批回滚）。
		// 分批前必须先去重：单条 IN 对重复工号天然只返回一份结果，
		// 但同一工号跨批出现会被重复查询，导致同一员工被重复授权、重复下发任务
		List<String> distinctBadges = reqDTO.getBadges().stream().distinct().collect(Collectors.toList());
		List<SmtStaff> staffList = OracleInBatchUtils.listInBatches(distinctBadges,
				badgeBatch -> smtStaffService.list(Wrappers.<SmtStaff>query().lambda()
						.ne(SmtStaff::getStatus, StaffStatusEnum.STAFF_STATUS_QUIT.getCode())
						.in(SmtStaff::getBadge, badgeBatch)));
		List<Long> staffIds = staffList.stream().map(SmtStaff::getId).distinct().collect(Collectors.toList());
		// 同一批人员可能已拥有其他权限组；统一查询其全部关联，才能选择设备最近一次授权的有效期。
		List<SmtStaffDeviceAuth> allStaffAuths = OracleInBatchUtils.listInBatches(staffIds,
				staffIdBatch -> smtStaffDeviceAuthService.list(Wrappers.<SmtStaffDeviceAuth>lambdaQuery()
						.in(SmtStaffDeviceAuth::getStaffId, staffIdBatch)));
		Map<Long, List<SmtStaffDeviceAuth>> authsByStaffId = allStaffAuths.stream()
				.collect(Collectors.groupingBy(SmtStaffDeviceAuth::getStaffId));
		List<Integer> involvedAuthIds = allStaffAuths.stream().map(SmtStaffDeviceAuth::getAuthId)
				.filter(Objects::nonNull).collect(Collectors.toList());
		involvedAuthIds.add(reqDTO.getAuthId());
		List<SmtDeviceAuthorityRelation> allAuthorityRelations = OracleInBatchUtils.listInBatches(
				involvedAuthIds.stream().distinct().collect(Collectors.toList()),
				authIdBatch -> smtDeviceAuthorityRelationService.list(Wrappers.<SmtDeviceAuthorityRelation>lambdaQuery()
						.in(SmtDeviceAuthorityRelation::getAuthorityId, authIdBatch)));

		Collection<SmtDeviceTask> deviceTaskList = new ArrayList<>();
		Collection<SmtIscDeviceTask> iscDeviceTaskList = new ArrayList<>();
		Collection<SmtStaffDeviceAuth> smtStaffDeviceAuthList = new ArrayList<>();
		for (SmtStaff staff : staffList) {
			if (existList.contains(staff.getId())) {
				// 该员工已存在此权限
				continue;
			}

			// 员工存在人脸图片
			if(StrUtil.isNotBlank(staff.getFacePicId())) {
				String gen = staff.getBadge() + SymbolConstants.MINUS + staff.getName();
				SmtStaffDeviceAuth staffDeviceAuth = new SmtStaffDeviceAuth();
				staffDeviceAuth.setStaffId(staff.getId());
				staffDeviceAuth.setAuthId(reqDTO.getAuthId());
				staffDeviceAuth.setCreateTime(new Date());
				staffDeviceAuth.setStartTime(validityWindow.getStartDateTime());
				staffDeviceAuth.setEndTime(validityWindow.getEndDateTime());
				List<SmtStaffDeviceAuth> effectiveStaffAuths = new ArrayList<>(
						authsByStaffId.getOrDefault(staff.getId(), Collections.emptyList()));
				effectiveStaffAuths.add(staffDeviceAuth);
				Map<String, PermissionValidityWindow> validityWindowsByDevice =
						PermissionValidityWindow.resolveByDevice(effectiveStaffAuths, allAuthorityRelations);

				for (String devCode : newDeviceList) {
					PermissionValidityWindow deviceValidityWindow = validityWindowsByDevice.get(devCode);
					if (deviceValidityWindow == null) {
						throw new SmartException("未找到设备" + devCode + "的权限有效期，无法下发");
					}
					// 判断设备是否为ISC同步的 是则把任务创建在新表中
					SmtDevice smtDevice = smtDeviceMapper.selectById(devCode);
					if (smtDevice == null) {
						log.info("设备{}不存在", devCode);
						continue;
					}

					// 判断是否ISC同步设备
					if (!StaffSyncEnum.YES.getCode().equals(smtDevice.getIsSync())) {
						SmtDeviceTask smtDeviceTask = new SmtDeviceTask();

						//生成随机序列
						String sNo = UUID.randomUUID().toString().replaceAll("-", "");

						if (StringUtils.isEmpty(smtDeviceTask.getSerialNo())) {
							smtDeviceTask.setSerialNo(sNo);
						}

						smtDeviceTask.setDeviceCode(devCode);
						smtDeviceTask.setCardNo(staff.getId().toString());
						smtDeviceTask.setGeneral(gen);
						smtDeviceTask.setServiceType(DeviceTaskConstants.CARD_STAFF_IMPORT);
						smtDeviceTask.setAction(DeviceTaskActionEnum.DOWN.getCode());
						smtDeviceTask.setCardType(SmtVisitorEnum.CAR_CARD_TYPE_1.getType());
						smtDeviceTask.setDeviceType(DeviceTaskConstants.CARD);
						smtDeviceTask.setImageId(staff.getFacePicId());
						smtDeviceTask.setStatus(DeviceTaskStatusEnum.INIT.getCode());
						smtDeviceTask.setOverTime(deviceValidityWindow.getOverTime());
						smtDeviceTask.setStartTime(deviceValidityWindow.getStartTime());
						smtDeviceTask.setCreateTime(LocalDateTime.now());

						deviceTaskList.add(smtDeviceTask);
					} else {
						SmtIscDeviceTask smtIscDeviceTask = new SmtIscDeviceTask();
						smtIscDeviceTask.setDeviceCode(devCode);
						smtIscDeviceTask.setCardNo(staff.getId().toString());
						smtIscDeviceTask.setGeneral(gen);
						smtIscDeviceTask.setServiceType(DeviceTaskConstants.CARD_STAFF_IMPORT);
						smtIscDeviceTask.setAction(DeviceTaskActionEnum.DOWN.getCode());
						smtIscDeviceTask.setDeviceType(DeviceTaskConstants.CARD);
						smtIscDeviceTask.setImageId(staff.getFacePicId());
						smtIscDeviceTask.setStatus(DeviceTaskStatusEnum.INIT.getCode());
						smtIscDeviceTask.setOverTime(deviceValidityWindow.getOverTime());
						smtIscDeviceTask.setStartTime(deviceValidityWindow.getStartTime());
						smtIscDeviceTask.setCreateTime(LocalDateTime.now());

						if (DeviceTaskConstants.CARD_VISITOR.equals(smtIscDeviceTask.getServiceType())) {
							smtIscDeviceTask.setOptUser("fkyy");
						} else {
							if (Objects.nonNull(SecurityUtils.getUser())) {
								smtIscDeviceTask.setOptUser(SecurityUtils.getUser().getUsername());
							} else {
								smtIscDeviceTask.setOptUser("sys");
							}
						}

						iscDeviceTaskList.add(smtIscDeviceTask);
					}

				}

				smtStaffDeviceAuthList.add(staffDeviceAuth);
			}else{
				noExist.add(staff.getBadge());
			}

		}

		if (!deviceTaskList.isEmpty()) {
			smtDeviceTaskService.saveBatch(deviceTaskList);
		}

		if (!iscDeviceTaskList.isEmpty()) {
			smtIscDeviceTaskService.saveBatch(iscDeviceTaskList);
		}

		if (!smtStaffDeviceAuthList.isEmpty()) {
			smtStaffDeviceAuthService.saveBatch(smtStaffDeviceAuthList);
		}

		return noExist;
	}

	@Override
	public void revokeDeviceAccess(Integer authorityId, String deviceId) {
        if(employeeAuthOperationAdapter!=null && employeeAuthOperationAdapter.revokeDevice(authorityId,deviceId)!=null)return;
		SmtDeviceAuthority authority = this.getById(authorityId);
		if (authority == null) {
			return;
		}
		List<String> devicesToRemove = Collections.singletonList(deviceId);
		if (DeviceAuthTypeEnum.PERSON.getCode().equals(authority.getType())) {
			updateStaffFaceAuthOptimized(authorityId, devicesToRemove, Collections.emptyList());
		} else if (DeviceAuthTypeEnum.VEHICLE.getCode().equals(authority.getType())) {
			updateVehicleAuthOptimized(authorityId, devicesToRemove, Collections.emptyList());
		}
	}

	/**
	 * 批量更新设备关联关系
	 *
	 * @param authorityId 权限ID
	 * @param parkIds 园区ID列表
	 * @param newDeviceIds 新的设备ID列表
	 */
	private void updateDeviceRelationsBatch(Integer authorityId, List<Integer> parkIds, List<String> newDeviceIds) {
		// 删除原来的设备关联数据
		LambdaQueryWrapper<SmtDeviceAuthorityRelation> deleteWrapper = Wrappers.<SmtDeviceAuthorityRelation>lambdaQuery()
			.eq(SmtDeviceAuthorityRelation::getAuthorityId, authorityId);

		// 只有当parkIds不为空时才添加园区ID条件
		if (CollUtil.isNotEmpty(parkIds)) {
			deleteWrapper.in(SmtDeviceAuthorityRelation::getParkId, parkIds);
		}

		smtDeviceAuthorityRelationService.remove(deleteWrapper);

		// 批量添加新的设备关联数据
		if (CollUtil.isNotEmpty(newDeviceIds)) {
			List<SmtDeviceAuthorityRelation> relationsToInsert = new ArrayList<>();
			for (String deviceId : newDeviceIds) {
				SmtDevice smtDevice = smtDeviceService.getById(deviceId);
				if (smtDevice != null) {
					SmtDeviceAuthorityRelation relation = new SmtDeviceAuthorityRelation();
					relation.setDeviceId(deviceId);
					relation.setAuthorityId(authorityId);
					relation.setParkId(smtDevice.getParkId());
					relationsToInsert.add(relation);
				}
			}

			if (!relationsToInsert.isEmpty()) {
				smtDeviceAuthorityRelationService.saveBatch(relationsToInsert);
			}
		}
	}

	/**
	 * 获取指定权限ID的所有车辆权限信息
	 *
	 * @param authId 权限ID
	 * @return 车辆权限信息列表
	 */
	private List<VehicleAuthDTO> getAllVehicleAuthByAuthId(Integer authId) {
		List<VehicleAuthDTO> allVehicleAuth = new ArrayList<>();

		// 使用分页查询但一次性获取所有数据
		Page<VehicleAuthDTO> page = new Page<>(1, 10000); // 设置足够大的页面大小
		IPage<VehicleAuthDTO> result = smtVehicleMapper.getVehicleAuthAll(page, authId);

		allVehicleAuth.addAll(result.getRecords());

		// 如果还有更多数据，继续查询
		while (result.getCurrent() < result.getPages()) {
			page.setCurrent(page.getCurrent() + 1);
			result = smtVehicleMapper.getVehicleAuthAll(page, authId);
			allVehicleAuth.addAll(result.getRecords());
		}

		return allVehicleAuth;
	}
}
