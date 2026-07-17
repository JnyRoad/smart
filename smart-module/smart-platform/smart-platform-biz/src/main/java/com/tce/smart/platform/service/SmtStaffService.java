package com.tce.smart.platform.service;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.platform.api.dto.SmtStaffDTO;
import com.tce.smart.platform.api.dto.req.EmpHrReqDTO;
import com.tce.smart.platform.api.dto.req.TempStaffEditReqDTO;
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

	Result getSmtStaffInfoById(String id);

	List<SmtStaffDTO> queryMobile(String mobile);

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

	Result outDormitory(SmtStaff smtStaff);

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

	/** 保密区批次专用入口，显式携带来源与接管意图，不改变通用下发语义。 */
	String updatePersonCardForSecurityDispatch(SmtStaff staff, String faceImage, String facePicId,
			List<SmtStaffDeviceAuth> staffDeviceAuths, String taskNum, String applyBadge,
			SecurityAuthDispatchContext context);

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
