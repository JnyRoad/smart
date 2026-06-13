package com.tce.smart.platform.core.service;

import com.tce.smart.platform.core.entity.SmtDevice;
import com.tce.smart.platform.core.entity.SmtStaff;

import java.util.List;

/**
 * 批量设备任务服务接口
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
public interface SmtBatchDeviceTaskService {

    /**
     * 批量创建员工人脸设备权限任务
     *
     * 功能说明：
     * 1. 为指定员工列表批量创建人脸权限任务
     * 2. 支持同时处理权限删除和新增操作
     * 3. 自动通过统一入口路由ISC和非ISC设备
     * 4. 提供详细的执行日志和性能统计
     * 5. 使用MyBatis Plus条件构建器优化设备查询
     *
     * 适用场景：
     * - 员工权限批量调整
     * - 组织架构变更后的权限重新分配
     * - 设备权限策略批量更新
     *
     * @param staffList 员工列表，要求每个员工必须有有效的人脸图片ID(facePicId)
     * @param delDevices 需要删除权限的设备ID列表，可以为空
     * @param addDevices 需要新增权限的设备ID列表，可以为空
     * @return int 成功创建的任务总数量
     *
     * @throws IllegalArgumentException 当员工列表为空或包含无效数据时
     */
    int createStaffFaceAuthTasks(List<SmtStaff> staffList,
                                List<String> delDevices,
                                List<String> addDevices);

    /**
     * 批量创建员工人脸设备权限任务（带任务记录编号）
     *
     * 功能说明：
     * 1. 在基础批量创建功能基础上，增加任务记录跟踪能力
     * 2. 为每个创建的任务生成详细的执行记录(SmtDeviceTaskDetail)
     * 3. 支持任务执行状态的精确跟踪和问题排查
     * 4. 使用延迟操作类型，适合大批量权限调整场景
     * 5. 使用MyBatis Plus条件构建器优化性能
     *
     * 与基础方法的区别：
     * - 使用DELAY_DEL和DELAY_DOWN操作类型
     * - 自动创建任务详情记录，便于后续跟踪
     * - 支持批量操作的进度监控
     *
     * 适用场景：
     * - 大规模权限调整需要详细跟踪的场景
     * - 需要生成操作审计记录的场景
     * - 分批次执行的权限下发任务
     *
     * @param staffList 员工列表，要求每个员工必须有有效的人脸图片ID(facePicId)
     * @param delDevices 需要删除权限的设备ID列表，可以为空
     * @param addDevices 需要新增权限的设备ID列表，可以为空
     * @param taskRecordNum 任务记录编号，用于关联所有相关的任务详情记录
     * @return int 成功创建的任务总数量
     */
    int createStaffFaceAuthTasksWithRecord(List<SmtStaff> staffList,
                                          List<String> delDevices,
                                          List<String> addDevices,
                                          String taskRecordNum);

    /**
     * 批量创建车辆设备权限任务
     *
     * 功能说明：
     * 1. 为指定车辆列表批量创建车辆通行权限任务
     * 2. 支持同时处理车辆权限的删除和新增操作
     * 3. 自动通过统一入口路由ISC和非ISC设备
     * 4. 使用延迟操作类型，适合批量车辆权限调整
     *
     * 车辆权限特点：
     * - 不需要图片信息，基于车牌号识别
     * - 支持多种车辆类型（员工车辆、公司车辆、访客车辆等）
     * - 设备类型固定为道闸设备(DeviceTaskConstants.CAR)
     *
     * 适用场景：
     * - 停车权限批量调整
     * - 车辆管理策略变更
     * - 临时车辆权限批量授权
     *
     * @param vehicleInfoList 车辆信息列表，包含车辆ID和车牌号
     * @param delDevices 需要删除权限的设备ID列表，可以为空
     * @param addDevices 需要新增权限的设备ID列表，可以为空
     * @return int 成功创建的任务总数量
     */
    int createVehicleAuthTasks(List<VehicleInfo> vehicleInfoList,
                              List<String> delDevices,
                              List<String> addDevices);

    /**
     * 根据设备类型查询特定类型的设备
     *
     * @param deviceType 设备类型
     * @param parkIds 园区ID列表
     * @return 设备列表
     */
    List<SmtDevice> getDevicesByTypeAndPark(Integer deviceType, List<Integer> parkIds);

    /**
     * 查询ISC设备列表
     *
     * @param deviceIds 设备ID列表
     * @return ISC设备列表
     */
    List<SmtDevice> getIscDevices(List<String> deviceIds);

    /**
     * 查询非ISC设备列表
     *
     * @param deviceIds 设备ID列表
     * @return 非ISC设备列表
     */
    List<SmtDevice> getNonIscDevices(List<String> deviceIds);

    /**
     * 车辆信息封装类
     *
     * 功能说明：
     * 1. 封装车辆权限任务所需的基本信息
     * 2. 提供车辆ID和车牌号的标准化存储格式
     * 3. 用于批量车辆权限任务创建时的参数传递
     *
     * 字段说明：
     * - vehicleId: 车辆的唯一标识ID，用作任务的cardNo
     * - vehiclePlate: 车牌号，用作任务的general字段和显示标识
     *
     * 使用示例：
     * new VehicleInfo("12345", "京A12345")
     */
    class VehicleInfo {
        private String vehicleId;
        private String vehiclePlate;

        public VehicleInfo(String vehicleId, String vehiclePlate) {
            this.vehicleId = vehicleId;
            this.vehiclePlate = vehiclePlate;
        }

        public String getVehicleId() {
            return vehicleId;
        }

        public String getVehiclePlate() {
            return vehiclePlate;
        }

        public void setVehicleId(String vehicleId) {
            this.vehicleId = vehicleId;
        }

        public void setVehiclePlate(String vehiclePlate) {
            this.vehiclePlate = vehiclePlate;
        }
    }
}