package com.tce.smart.platform.service;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.platform.api.dto.SmtStaffDTO;
import com.tce.smart.platform.api.dto.req.EmpHrReqDTO;
import com.tce.smart.platform.api.dto.req.AdminStaffPhoneUpdateReqDTO;
import com.tce.smart.platform.api.dto.req.AdminStaffUpdateReqDTO;
import com.tce.smart.platform.api.dto.req.AdminTemporaryStaffQueryReqDTO;
import com.tce.smart.platform.api.dto.req.TempStaffEditReqDTO;
import com.tce.smart.platform.api.dto.resp.InternalStaffAccountRespDTO;
import com.tce.smart.platform.api.dto.resp.AdminStaffDetailRespDTO;
import com.tce.smart.platform.api.dto.resp.AdminTemporaryStaffRespDTO;
import com.tce.smart.platform.api.dto.resp.AdminTemporaryStaffDetailRespDTO;
import com.tce.smart.platform.api.dto.resp.StaffLookupRespDTO;
import com.tce.smart.platform.api.dto.resp.StaffSelfCheckInProfileRespDTO;
import com.tce.smart.platform.api.dto.resp.StaffPartInfo;
import com.tce.smart.platform.core.dto.*;
import com.tce.smart.platform.core.entity.*;
import com.tce.smart.platform.core.entity.ext.SecurityPersonRelationExt;
import com.tce.smart.platform.core.vo.*;

import java.util.List;

/**
 * 员工表
 *
 * @author 齐佩
 * @date 2019-04-13 18:18:42
 */
public interface SmtStaffService extends IService<SmtStaff> {

	Result addStaff(ApplicationStaffDTO smtStaff);

	MyDormitoryVO myDormitory(SmtStaff smtStaff);

	List<SmtStaff> findStaffByMobileForLogin(String mobile);

	SmtStaff getByPhoneAndName(String phone, String name);

	StaffInfoVO getSmtStaffInfoByPhone(String phone);

	StaffInfoVO getSmtStaffInfoByPhone(String phone, String name);

	Boolean saveTemporaryStaff(TempStaffEditReqDTO tempStaff, SmtDormitoryStaffService dormitoryStaffService);

	void syncFaceImg();

	Boolean updateTemporaryStaff(TempStaffEditReqDTO tempStaff);

	IPage<StaffListVO> getSmtStaffPage(Page page, SearchStaffDTO smtStaff);

	StaffInfoVO getBaseinfoById(String badge);

	IPage<SmtVehicle> getMyVehicle(Page page, String badge);

	Result addVehiclePark(ApplyAuthDTO smtVehicleApply);

	List<VehicleApplyVO> getVehiclePark(String vehiclePlate);

	VehicleParkDetailVO getVehicleParkById(Integer id);

	Result updatePhone(SmtStaff smtStaff);

	SmtStaff getStaffByBadgeAll(String badge);

	SmtStaff getStaffByBadge(String badge);

	SmtStaff getStaffByBadge(String badge, String compId);

	List<SmtStaff> getStaffByBadges(List<String> badges, String compId);

	IPage<SmtStaff> getTempList(Page page, TempStaffEditReqDTO reqDTO);

	IPage<StaffNODormitoryVO> quetyStaffNODormitory(Page page, SearchStaffDTO smtStaff);

	StaffInfoVO getSmtStaffInfoByBadge(String badge);

	Result addVehicle(AddVehicleDTO addVehicleDTO);

	void synDeleteUserInfo(SmtStaff smtStaff);

	Result getQrcode(String badge);

	void syncStaff(EmpHrVO empHr, SmtDormitoryStaffService dormitoryStaffService);

	/**
	 * app完善人脸信息
	 *
	 * @param perfectDTO 身份证、人脸照片信息
	 * @return true-操作成功，false-操作失败
	 */
	Result<Boolean> perfectFace(StaffPerfectDTO perfectDTO);

	/**
	 * app调用，检查是否需要完善人脸信息
	 *
	 * @param badge 员工工号
	 * @return true-完善，false-不完善
	 */
	Boolean checkPerfectInfo(String badge);

	/**
	 * 根据员工工号获基本信息
	 *
	 * @param badge 员工工号
	 * @return 员工信息
	 */
	SmtStaff getSimpleSttaffByBadge(String badge);

	/**
	 * 按管理员可见园区查询员工最小信息。
	 *
	 * @param badge 工号关键字
	 * @param parkIds 当前管理员可见园区
	 * @return 不含个人敏感信息的员工列表
	 */
	List<StaffLookupRespDTO> searchStaffForAdmin(String badge, List<Integer> parkIds);

	/**
	 * 按管理员可见园区查询员工受控详情。
	 *
	 * @param staffId 员工主键
	 * @param parkIds 当前管理员可见园区
	 * @return 不含证件、联系方式、地址和人脸资料的员工详情；越园区或不存在时返回空
	 */
	AdminStaffDetailRespDTO getAdminStaffDetail(Long staffId, List<Integer> parkIds);

	/**
	 * 查询当前管理员园区内可批量离职的临时员工最小资料。
	 *
	 * @param badges 工号集合
	 * @param parkIds 当前管理员可见园区
	 * @return 仅含主键、工号和姓名的临时员工记录
	 */
	List<AdminTemporaryStaffRespDTO> searchTemporaryStaffForAdmin(List<String> badges, List<Integer> parkIds);

	/** 按管理员园区范围分页查询临时人员的最小资料。 */
	IPage<AdminTemporaryStaffDetailRespDTO> getTemporaryStaffPageForAdmin(Page page,
			AdminTemporaryStaffQueryReqDTO request, List<Integer> parkIds);

	/** 按管理员园区范围查询一名临时人员的最小资料。 */
	AdminTemporaryStaffDetailRespDTO getTemporaryStaffDetailForAdmin(Long staffId, List<Integer> parkIds);

	/** 后台修改手机号前验证目标员工属于当前管理员园区。 */
	Boolean updateStaffPhoneForAdmin(AdminStaffPhoneUpdateReqDTO request, List<Integer> parkIds);

	/** 后台仅能修改最小基础字段，并在服务端验证目标员工园区。 */
	Boolean updateStaffForAdmin(AdminStaffUpdateReqDTO request, List<Integer> parkIds);

	/**
	 * 查询当前员工入住流程可展示的资料摘要。
	 *
	 * @param badge 已认证主体的工号
	 * @return 仅含姓名、资料完整状态和脱敏证件号的资料摘要
	 */
	StaffSelfCheckInProfileRespDTO getCheckInProfileForBadge(String badge);

	/**
	 * 查询可用于本人入住的在职员工资料。
	 *
	 * @param badge 已认证主体的工号
	 * @return 在职员工，不存在或已离职时返回空
	 */
	SmtStaff getActiveStaffByBadge(String badge);

	/**
	 * 按工号查询内部账号识别信息。
	 *
	 * @param badge 工号
	 * @return 仅含内部账号识别所需字段的响应
	 */
	InternalStaffAccountRespDTO getInternalAccountByBadge(String badge);

	/**
	 * 根据员工ID获取员工信息
	 *
	 * @param staffId 员工ID
	 * @return 员工信息
	 */
	SmtStaff getSimpleSttaffById(String staffId);


	/**
	 * 根据员工ID列表 获取员工信息
	 *
	 * @param staffIds 员工ID
	 * @return 员工信息
	 */
	List<SmtStaff> getSimpleSttaffByIds(List<String> staffIds);

	/**
	 * 人脸搜索员工信息
	 *
	 * @param facePic  人脸base64字符
	 * @param deviceNo 设备编号
	 * @return 员工信息
	 */
	SmtStaff faceSearchForLogin(String facePic, String deviceNo);

	/**
	 * 人脸对比
	 * @param perfectDTO
	 * @return
	 */
	Boolean faceSearchForCompare(StaffPerfectDTO perfectDTO);
	/**
	 * 设备人员删除、添加任务操作
	 *
	 * @param smtStaff 员工信息
	 */
	void addDeviceTask(SmtStaff smtStaff, Integer action);

	/**
	 * 同步员工头像到C6
	 *
	 * @param badge 员工工号
	 * @return true-成功,false-失败
	 */
	boolean synStaffFaceImage(String badge);

	Boolean syncIscPersonFace(String badge, Integer parkId, String imageId);

	Boolean retryFailedIscPersonFaceSync();

	List<CheckFacePicVO> checkFacePic(CheckFacePicDTO dto);

	String upload(CheckFacePicDTO check);

	ToC6ePhoto toC6ePhoto(ToC6ePhoto toC6ePhoto);

	Boolean register(StaffRegisterDTO staffRegisterDTO);

	Result getAuthInfo(String id);


	Page<StaffListVO> getTOStaffPage(Page page, SearchToStaffDTO searchToStaffDTO);

	Result getToStaffInfoById(String id);

	List<SmtPark> getStaffPark(String staffBadge);

	Result addStaffToHR(ApplicationStaffDTO smtStaff);

	/**
	 * 获得当月入职员工
	 *
	 * @return
	 */
	List<SmtStaff> getNewStaff();

	/**
	 * 获得往月入职员工
	 *
	 * @return
	 */
	List<SmtStaff> getSeniorStaff();

	/**
	 * 根据员工编号模糊查询员工信息
	 *
	 * @return 响应 (name badge detname)三个信息
	 */
	List<StaffPartInfo> getStaffInfo(String staffBadge);

	/**
	 * 分页查询员工信息
	 *
	 * @param page
	 * @return
	 */
	IPage<EmpHrReqDTO> getStaffList(Page page);

	/**
	 * 后台修改员工手机号
	 *
	 * @param staffId
	 * @param newPhone
	 * @return
	 */
	Boolean updateStaffPhone(Long staffId, String newPhone);

	/**
	 * 通过工号查询未离职员工
	 *
	 * @param staffBadge
	 * @return
	 */
	SmtStaff getStffNoQuitByBadge(String staffBadge);


	/**
	 * 获取待充值员工
	 *
	 * @return
	 */
	List<SmtStaff> getSeniorRechargeStaff();

	List<StaffListVO> remoteSyncStaffInfo(Integer parkId, String createTime);

	/**
	 * 根据员工号获取卡片
	 *
	 * @param empNo
	 * @return
	 */
	String getEmpCard(String empNo);

	Integer createStaffPhotoUploadRecord(SmtStaff one);

	String updatePersonCard(SmtStaff staff, String faceImage, String facePicId, List<SmtStaffDeviceAuth> staffDeviceAuths,
							String taskNum, String applyBadge);

	void savePersonCardTask(Integer actionType, long startTime, long endTime, SmtStaff smtStaff, List<SmtDeviceAuthorityRelation> deviceAuthList);

	IPage<SecurityAllStaffListDTO> getStaffPage(Page page, SecurityPersonRelationExt reqDTO);

	void faceStorage(SmtStaff staff, String facePicId, Integer recordId, String faceImage);

	/**
	 * 保存员工数据到C6
	 * @param staff
	 * @param organizeRelation
	 */
	void saveToC6(SmtStaff staff, SmtOrganizeRelation organizeRelation);

	/**
	 * 批量将员工离职状态改为在职
	 * @param tempStaffs
	 * @return
	 */
	Boolean batchUpdateTempStatus(List<String> tempStaffs);

	/**
	 * 工号忽略大小写查询
	 * @param badge
	 * @return
	 */
	SmtStaff getStaffIgnoreCase(String badge);
}
