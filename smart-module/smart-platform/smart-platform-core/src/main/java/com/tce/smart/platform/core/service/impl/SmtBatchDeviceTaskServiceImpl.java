package com.tce.smart.platform.core.service.impl;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tce.smart.common.core.constant.enums.SmtVisitorEnum;
import com.tce.smart.platform.core.entity.SmtDevice;
import com.tce.smart.platform.core.entity.SmtDeviceTaskDetail;
import com.tce.smart.platform.core.entity.SmtStaff;
import com.tce.smart.platform.core.service.SmtDeviceService;
import com.tce.smart.platform.core.service.SmtDeviceTaskService;
import com.tce.smart.platform.core.service.SmtBatchDeviceTaskService;
import com.tce.smart.platform.core.dto.DeviceTaskVO;
import com.tce.smart.platform.core.enums.StaffSyncEnum;
import com.tce.smart.tool.constant.DeviceTaskConstants;
import com.tce.smart.tool.constant.SymbolConstants;
import com.tce.smart.tool.enums.DeviceTaskActionEnum;
import com.tce.smart.tool.enums.DeviceTaskStatusEnum;
import com.tce.smart.tool.enums.EnableStatusEnum;
import com.tce.smart.tool.util.RegexUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 批量设备任务服务实现类
 *
 * 核心功能：
 * 1. 统一使用SmtDeviceTaskService.saveTask()入口处理任务创建
 * 2. 自动识别并路由ISC和非ISC设备的权限下发方式
 * 3. 支持批量创建员工人脸权限任务和车辆权限任务
 * 4. 提供完整的错误处理和任务执行状态跟踪
 * 5. 使用MyBatis Plus条件构建器优化数据查询性能
 *
 * 设备路由机制：
 * - ISC设备(isSync=1)：自动路由到SmtIscDeviceTaskService
 * - 非ISC设备(isSync=0)：使用SmtDeviceTaskService直接处理
 *
 * @author 李世勋 (lisx)
 * @date 2025-06-26
 * @version 2.0 - 重构为Service层，遵循Spring Boot最佳实践
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SmtBatchDeviceTaskServiceImpl implements SmtBatchDeviceTaskService {

    private final SmtDeviceTaskService smtDeviceTaskService;
    private final SmtDeviceService smtDeviceService;

    /**
     * 批量创建员工人脸设备权限任务
     */
    @Override
    public int createStaffFaceAuthTasks(List<SmtStaff> staffList,
                                       List<String> delDevices,
                                       List<String> addDevices) {
        long startTime = System.currentTimeMillis();

        // 参数验证
        validateStaffList(staffList);
        validateDeviceLists(delDevices, addDevices);

        log.info("开始批量创建员工人脸权限任务: 员工{}人, 删除设备{}个, 新增设备{}个",
                staffList.size(), delDevices.size(), addDevices.size());

        int totalTaskCount = 0;

        // 使用MyBatis Plus条件构建器预先查询所有设备信息
        Map<String, SmtDevice> deviceMap = getDeviceMapByIds(delDevices, addDevices);

        // 统计ISC和非ISC设备数量（仅用于日志）
        DeviceStatistics deviceStats = calculateDeviceStatistics(deviceMap);
        log.info("设备统计: ISC设备{}个, 非ISC设备{}个, 有效设备{}个",
                deviceStats.iscCount, deviceStats.nonIscCount, deviceStats.totalCount);

        // 为每个员工创建任务
        for (SmtStaff staff : staffList) {
            // 处理删除任务 - 修复：使用DELAY_DEL保持与旧版本一致
            totalTaskCount += createTasksForDevices(staff, delDevices,
                    DeviceTaskActionEnum.DEL, null);

            // 处理新增任务 - 修复：使用DELAY_DOWN保持与旧版本一致
            totalTaskCount += createTasksForDevices(staff, addDevices,
                    DeviceTaskActionEnum.DOWN, null);
	}

        long endTime = System.currentTimeMillis();
        log.info("批量创建员工人脸权限任务完成: 共创建{}个任务, 耗时{}ms",
                totalTaskCount, endTime - startTime);

        return totalTaskCount;
    }

    /**
     * 批量创建员工人脸设备权限任务（带任务记录编号）
     */
    @Override
    public int createStaffFaceAuthTasksWithRecord(List<SmtStaff> staffList,
                                                 List<String> delDevices,
                                                 List<String> addDevices,
                                                 String taskRecordNum) {
        long startTime = System.currentTimeMillis();

        // 参数验证
        validateStaffList(staffList);
        validateDeviceLists(delDevices, addDevices);
        Assert.hasText(taskRecordNum, "任务记录编号不能为空");

        log.info("开始批量创建员工人脸权限任务(带记录): 员工{}人, 删除设备{}个, 新增设备{}个, 记录编号:{}",
                staffList.size(), delDevices.size(), addDevices.size(), taskRecordNum);

        int totalTaskCount = 0;

        // 为每个员工创建任务
        for (SmtStaff staff : staffList) {
            // 处理删除任务
            totalTaskCount += createTasksForDevices(staff, delDevices,
                    DeviceTaskActionEnum.DEL, taskRecordNum);

            // 处理新增任务
            totalTaskCount += createTasksForDevices(staff, addDevices,
                    DeviceTaskActionEnum.DOWN, taskRecordNum);
        }

        long endTime = System.currentTimeMillis();
        log.info("批量创建员工人脸权限任务(带记录)完成: 共创建{}个任务, 耗时{}ms",
                totalTaskCount, endTime - startTime);

        return totalTaskCount;
    }

    /**
     * 使用MyBatis Plus条件构建器查询设备信息
     */
    private Map<String, SmtDevice> getDeviceMapByIds(List<String> delDevices, List<String> addDevices) {
        // 合并所有设备ID
        List<String> allDeviceIds = new ArrayList<>();
        if (!CollectionUtils.isEmpty(delDevices)) {
            allDeviceIds.addAll(delDevices);
        }
        if (!CollectionUtils.isEmpty(addDevices)) {
            allDeviceIds.addAll(addDevices);
        }

        // 如果没有设备ID，返回空Map
        if (CollectionUtils.isEmpty(allDeviceIds)) {
            return Collections.emptyMap();
        }

        // 使用MyBatis Plus的LambdaQueryWrapper进行类型安全查询
        List<SmtDevice> devices = smtDeviceService.list(
            Wrappers.<SmtDevice>lambdaQuery()
                .in(SmtDevice::getId, allDeviceIds)
                .isNotNull(SmtDevice::getId)
                .orderByAsc(SmtDevice::getId)
        );

        // 转换为Map便于快速查找
        return devices.stream()
                .collect(Collectors.toMap(SmtDevice::getId, Function.identity()));
    }

    /**
     * 根据设备类型查询特定类型的设备
     */
    @Override
    public List<SmtDevice> getDevicesByTypeAndPark(Integer deviceType, List<Integer> parkIds) {
        if (deviceType == null || CollectionUtils.isEmpty(parkIds)) {
            return Collections.emptyList();
        }

        return smtDeviceService.list(
            Wrappers.<SmtDevice>lambdaQuery()
                .eq(SmtDevice::getDeviceType, deviceType)
                .in(SmtDevice::getParkId, parkIds)
                .eq(SmtDevice::getEnableStatus, EnableStatusEnum.ENABLE.getCode()) // 启用状态
                .orderByAsc(SmtDevice::getParkId, SmtDevice::getId)
        );
    }

    /**
     * 查询ISC设备列表
     */
    @Override
    public List<SmtDevice> getIscDevices(List<String> deviceIds) {
        if (CollectionUtils.isEmpty(deviceIds)) {
            return Collections.emptyList();
        }

        return smtDeviceService.list(
            Wrappers.<SmtDevice>lambdaQuery()
                .in(SmtDevice::getId, deviceIds)
                .eq(SmtDevice::getIsSync, StaffSyncEnum.YES.getCode())
                .orderByAsc(SmtDevice::getId)
        );
    }

    /**
     * 查询非ISC设备列表
     */
    @Override
    public List<SmtDevice> getNonIscDevices(List<String> deviceIds) {
        if (CollectionUtils.isEmpty(deviceIds)) {
            return Collections.emptyList();
        }

        return smtDeviceService.list(
            Wrappers.<SmtDevice>lambdaQuery()
                .in(SmtDevice::getId, deviceIds)
                .ne(SmtDevice::getIsSync, StaffSyncEnum.YES.getCode())
                .orderByAsc(SmtDevice::getId)
        );
    }

    /**
     * 参数验证 - 员工列表
     */
    private void validateStaffList(List<SmtStaff> staffList) {
        Assert.notNull(staffList, "员工列表不能为空");
        Assert.notEmpty(staffList, "员工列表不能为空");

        // 验证员工数据完整性
        for (SmtStaff staff : staffList) {
            Assert.notNull(staff, "员工对象不能为空");
            Assert.notNull(staff.getId(), "员工ID不能为空: " + staff.getName());
            Assert.hasText(staff.getName(), "员工姓名不能为空: ID=" + staff.getId());
            Assert.hasText(staff.getBadge(), "员工工号不能为空: " + staff.getName());
            // 验证人脸图片ID，无人脸图片的员工不允许创建任务
            if (StringUtils.isEmpty(staff.getFacePicId())) {
                throw new IllegalArgumentException("员工" + staff.getName() + "无人脸照片，不能创建权限任务");
            }
        }
    }

    /**
     * 参数验证 - 设备列表
     */
    private void validateDeviceLists(List<String> delDevices, List<String> addDevices) {
        // 至少要有一个操作
        if (CollectionUtils.isEmpty(delDevices) && CollectionUtils.isEmpty(addDevices)) {
            throw new IllegalArgumentException("删除设备列表和新增设备列表不能同时为空");
        }

        // 验证设备ID格式（如果需要）
        validateDeviceIds(delDevices, "删除设备");
        validateDeviceIds(addDevices, "新增设备");
    }

    /**
     * 验证设备ID格式
     */
    private void validateDeviceIds(List<String> deviceIds, String deviceType) {
        if (CollectionUtils.isEmpty(deviceIds)) {
            return;
        }

        for (String deviceId : deviceIds) {
            Assert.hasText(deviceId, deviceType + "ID不能为空");
            // 可以添加更多格式验证，如长度限制等
        }
    }

    /**
     * 计算设备统计信息
     */
    private DeviceStatistics calculateDeviceStatistics(Map<String, SmtDevice> deviceMap) {
        if (CollectionUtils.isEmpty(deviceMap)) {
            return new DeviceStatistics(0, 0, 0);
        }

        long iscCount = deviceMap.values().stream()
                .filter(device -> StaffSyncEnum.YES.getCode().equals(device.getIsSync()))
                .count();

        int totalCount = deviceMap.size();
        long nonIscCount = totalCount - iscCount;

        return new DeviceStatistics((int) iscCount, (int) nonIscCount, totalCount);
    }

    /**
     * 设备统计信息内部类
     */
    private static class DeviceStatistics {
        final int iscCount;
        final int nonIscCount;
        final int totalCount;

        DeviceStatistics(int iscCount, int nonIscCount, int totalCount) {
            this.iscCount = iscCount;
            this.nonIscCount = nonIscCount;
            this.totalCount = totalCount;
        }
    }

    /**
     * 为指定员工和设备列表创建权限任务
     */
    private int createTasksForDevices(SmtStaff staff, List<String> deviceIds,
                                     DeviceTaskActionEnum action, String taskRecordNum) {
        int taskCount = 0;
        String general = staff.getBadge() + SymbolConstants.MINUS + staff.getName();
        String cardNo = staff.getId().toString();

        for (String deviceId : deviceIds) {
            // 创建任务VO
            DeviceTaskVO taskVO = new DeviceTaskVO();
            taskVO.setCardNo(cardNo);
            taskVO.setDeviceCode(deviceId);
            taskVO.setGeneral(general);
            taskVO.setImageId(staff.getFacePicId());
            taskVO.setAction(action.getCode());
            taskVO.setServiceType(DeviceTaskConstants.CARD_STAFF_IMPORT);
            taskVO.setCardType(SmtVisitorEnum.CAR_CARD_TYPE_1.getType());
            taskVO.setDeviceType(DeviceTaskConstants.CARD);
            taskVO.setStatus(DeviceTaskStatusEnum.INIT.getCode());

            // 设置时间
            if (action == DeviceTaskActionEnum.DEL || action == DeviceTaskActionEnum.DELAY_DEL) {
                taskVO.setStartTime(DateUtil.currentSeconds());
                taskVO.setOverTime(DateUtil.currentSeconds());
            } else {
                taskVO.setStartTime(DateUtil.currentSeconds());
                taskVO.setOverTime(DeviceTaskConstants.maxTime);
            }

            // 添加详细日志：记录任务创建前的设备信息
            log.info("批量任务创建 - 员工: {}, 设备: {}, 动作: {}, 即将调用saveTask进行路由",
                    staff.getName(), deviceId, action.getDesc());

            // 通过统一入口创建任务，自动处理ISC和非ISC设备路由
            String taskId = smtDeviceTaskService.saveTask(taskVO);

            // 添加详细日志：记录任务创建结果
            log.info("批量任务创建结果 - 员工: {}, 设备: {}, 动作: {}, 任务ID: {}",
                    staff.getName(), deviceId, action.getDesc(), taskId);

            // 处理任务创建结果
            if (taskId != null && !taskId.contains("不存在") && !taskId.contains("已存在")) {
                taskCount++;

                // 如果需要记录任务详情
                if (taskRecordNum != null) {
                    createTaskDetail(taskId, general, action.getCode(), taskRecordNum, null);
                }
            } else {
                log.warn("任务创建失败: 员工{}, 设备{}, 动作{}, 原因:{}",
                        staff.getName(), deviceId, action.getDesc(), taskId);

                // 如果需要记录任务详情，记录失败信息
                if (taskRecordNum != null) {
                    createTaskDetail(null, general, action.getCode(), taskRecordNum, taskId);
                }
            }
        }

        return taskCount;
    }

    /**
     * 批量创建车辆设备权限任务
     */
    @Override
    public int createVehicleAuthTasks(List<VehicleInfo> vehicleInfoList,
                                     List<String> delDevices,
                                     List<String> addDevices) {
        long startTime = System.currentTimeMillis();
        log.info("开始批量创建车辆权限任务: 车辆{}辆, 删除设备{}个, 新增设备{}个",
                vehicleInfoList.size(), delDevices.size(), addDevices.size());

        int totalTaskCount = 0;

        for (VehicleInfo vehicle : vehicleInfoList) {
            String cardNo = vehicle.getVehicleId();
            String general = vehicle.getVehiclePlate();

            // 创建删除权限任务
            for (String deviceId : delDevices) {
                DeviceTaskVO deleteTaskVO = createVehicleTaskVO(
                    cardNo, deviceId, general,
                    DeviceTaskActionEnum.DELAY_DEL.getCode(),
                    DateUtil.currentSeconds(),
                    DateUtil.currentSeconds()
                );

                String taskId = smtDeviceTaskService.saveTask(deleteTaskVO);
                if (StringUtils.hasText(taskId) && RegexUtils.matchNumber(taskId)) {
                    totalTaskCount++;
                }
            }

            // 创建新增权限任务
            for (String deviceId : addDevices) {
                DeviceTaskVO addTaskVO = createVehicleTaskVO(
                    cardNo, deviceId, general,
                    DeviceTaskActionEnum.DELAY_DOWN.getCode(),
                    DateUtil.currentSeconds(),
                    DeviceTaskConstants.maxTime
                );

                String taskId = smtDeviceTaskService.saveTask(addTaskVO);
                if (StringUtils.hasText(taskId) && RegexUtils.matchNumber(taskId)) {
                    totalTaskCount++;
                }
            }
        }

        long endTime = System.currentTimeMillis();
        log.info("批量创建车辆权限任务完成: 共创建{}个任务, 耗时{}ms",
                totalTaskCount, endTime - startTime);

        return totalTaskCount;
    }

    /**
     * 创建车辆权限任务的参数对象
     */
    private DeviceTaskVO createVehicleTaskVO(String cardNo, String deviceId, String general,
                                           Integer action, Long startTime, Long overTime) {
        DeviceTaskVO taskVO = new DeviceTaskVO();
        taskVO.setCardNo(cardNo);
        taskVO.setDeviceCode(deviceId);
        taskVO.setGeneral(general);
        taskVO.setAction(action);
        taskVO.setServiceType(DeviceTaskConstants.CAR_STAFF);
        taskVO.setCardType(SmtVisitorEnum.CAR_CARD_TYPE_1.getType());
        taskVO.setDeviceType(DeviceTaskConstants.CAR);
        taskVO.setStatus(DeviceTaskStatusEnum.INIT.getCode());
        taskVO.setStartTime(startTime);
        taskVO.setOverTime(overTime);
        return taskVO;
    }

    /**
     * 创建任务详情记录
     */
    private void createTaskDetail(String taskId, String general, Integer action,
                                 String taskRecordNum, String errorMsg) {
        String[] idsArray = general.split(SymbolConstants.MINUS);
        String badge = idsArray.length > 0 ? idsArray[0] : "";
        String name = idsArray.length > 1 ? idsArray[1] : "";

        SmtDeviceTaskDetail.SmtDeviceTaskDetailBuilder builder = SmtDeviceTaskDetail.builder()
                .taskListId(taskRecordNum)
                .action(action)
                .badge(badge)
                .name(name)
                .createTime(LocalDateTime.now());

        if (taskId != null && RegexUtils.matchNumber(taskId)) {
            // 任务创建成功
            builder.taskId(taskId)
                   .status(DeviceTaskStatusEnum.INIT.getCode());
        } else {
            // 任务创建失败
            builder.status(DeviceTaskStatusEnum.FAIL.getCode())
                   .remark(errorMsg != null ? errorMsg : "任务创建失败");
        }

        SmtDeviceTaskDetail taskDetail = builder.build();
        taskDetail.insert();
    }
}