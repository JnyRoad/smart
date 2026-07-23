package com.tce.smart.platform.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.data.api.dto.ehrview.resp.EvwEmphrYsRespDTO;
import com.tce.smart.data.api.dto.ehrview.resp.EvwJjitemRespDTO;
import com.tce.smart.data.api.dto.msg.req.DimissionMsgReqDTO;
import com.tce.smart.data.api.dto.temporary.req.EleaveJjitemReqDTO;
import com.tce.smart.data.api.feign.ehrview.RemoteEvwEmphrYsService;
import com.tce.smart.data.api.feign.ehrview.RemoteEvwJjitemService;
import com.tce.smart.data.api.feign.msg.RemoteSmsManageService;
import com.tce.smart.data.api.feign.temporary.RemoteEleaveJjitemService;
import com.tce.smart.platform.core.dto.AppMsgPushDTO;
import com.tce.smart.platform.core.dto.DeviceTaskVO;
import com.tce.smart.platform.core.dto.LeaveHandoverDTO;
import com.tce.smart.platform.core.entity.*;
import com.tce.smart.platform.core.mapper.SmtLeaveApplicationMapper;
import com.tce.smart.platform.core.mapper.SmtLeaveHandoverMapper;
import com.tce.smart.platform.core.mapper.SmtVehicleStaffMapper;
import com.tce.smart.platform.core.service.SmtDeviceTaskService;
import com.tce.smart.platform.core.service.SmtIscStaffCardService;
import com.tce.smart.platform.service.*;
import com.tce.smart.tool.constant.ApproveListTypeConstants;
import com.tce.smart.tool.constant.DeviceTaskConstants;
import com.tce.smart.tool.constant.LeaveHandoverConstants;
import com.tce.smart.tool.enums.*;
import com.tce.smart.tool.exception.TCEException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 工作交接
 *
 * @author 王艳勇
 * @date 2019-04-15 11:33:51
 */
@Slf4j
@Service
@AllArgsConstructor
public class SmtLeaveHandoverServiceImpl extends ServiceImpl<SmtLeaveHandoverMapper, SmtLeaveHandover> implements SmtLeaveHandoverService {
    private final SmtLeaveApplicationMapper leaveApplicationMapper;
    private final RemoteEvwJjitemService remoteEvwJjitemService;
    private final RemoteEleaveJjitemService remoteEleaveJjitemService;
    private final SmtVehicleStaffMapper vehicleStaffMapper;
    private final SmtVehicleService vehicleService;
    private final ApproveListService approveListService;
    private final RemoteSmsManageService remoteSmsManageService;
    private final SmtStaffDeviceAuthService smtStaffDeviceAuthService;
    private final SmtDeviceTaskService smtDeviceTaskService;
    private final RemoteEvwEmphrYsService remoteEvwEmphrYsService;
    private final SmtProcessRecordService smtProcessRecordService;
    private final IAppMsgPushService appMsgPushService;
    private final SmtDormitoryStaffService dormitoryStaffService;
    private final SmtParkBuService smtParkBuService;
	private final SmtDeviceAuthorityRelationService smtDeviceAuthorityRelationService;
	private final SmtIscStaffCardService smtIscStaffCardService;
    /**
     * 初始化交接信息
     * @param leaveApplication 离职信息
     * @return
     */
    @Override
    @Transactional(rollbackFor=Exception.class)
    public Result initLeaveHandover(SmtLeaveApplication leaveApplication) {
	Result<List<EvwJjitemRespDTO>> result = remoteEvwJjitemService.info(leaveApplication.getEzid(), SecurityConstants.FROM_IN, SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED);
		if(!result.isSuccess() || (result.isSuccess() && CollUtil.isEmpty(result.getData()))) {
			return new Result(false,"该员工所在的人事区域，没有设置离职交接项，请联系管理员设置");
		}
        List<EvwJjitemRespDTO> evwJjitemList = result.getData();

        List<SmtLeaveHandover> list = this.LeaveHandoverHandle(evwJjitemList, leaveApplication);
        int num = this.baseMapper.initLeaveHandover(list);
		return new Result(num > 0);
    }

    /**
     * 开始工作交接
     */
    @Override
    public boolean startLeaveHandover(String processId) {
	    SmtLeaveApplication leaveApplication = leaveApplicationMapper.getLeaveApplicationByProcessId(processId);
	    int num = 0;
	    if(leaveApplication.getApproveStatus().equals(LeaveApplicationStatusEnum.APPROVED.getCode())) {
		this.approveListHandle(leaveApplication.getId(),leaveApplication.getProcessId());
		boolean result = smtProcessRecordService.getHandoverRecord(leaveApplication.getProcessId(), leaveApplication.getBadge());
	        // 用户点击开始工作交接按钮
	        if(!result) {
		this.saveProcessRecord(leaveApplication);
	        }
	        num = leaveApplicationMapper.updateStatus(LeaveApplicationStatusEnum.START.getCode(), processId);
	        if(num > 0) {
		List<SmtLeaveHandover> leaveHandoverList = this.baseMapper.getApproveList(leaveApplication.getId());
		if(CollectionUtil.isNotEmpty(leaveHandoverList)) {
			leaveHandoverList.forEach(v->{
				//推送App消息
					AppMsgPushDTO appMsgPushDTO = new AppMsgPushDTO();
					appMsgPushDTO.setBadge(v.getJjr());
					appMsgPushDTO.setBussiessId(leaveApplication.getProcessId());
					appMsgPushDTO.setTemplateCode(SmsTemplateEnum.APP_PUSH_2304.getCode());
					appMsgPushDTO.setExtraParam("leaveStatus=" + leaveApplication.getLeaveStatus() + "||" + "approveStatus=" +  leaveApplication.getApproveStatus());
					appMsgPushService.pushAppMsg(appMsgPushDTO);
			});
		}
	        }
	    }
        return num > 0;
    }

    /**
     * 注消人员信息
     * @param cardNo
     * @param parkIds
     */
    private void Logout(Long cardNo,List<Integer> parkIds) {
		List<SmtDeviceAuthorityRelation> selectList = smtDeviceAuthorityRelationService
				.getMulRelationAuth(cardNo, parkIds, BusinessAuthorityEnum.STAFF_FACE.getCode(), DeviceAuthorityEnum.STAFF);
        DeviceTaskVO deviceTaskVO = null;
        for (int i = 0; i < selectList.size(); i++) {
            deviceTaskVO = new DeviceTaskVO();
		deviceTaskVO.setAction(DeviceTaskConstants.DEL);
			deviceTaskVO.setDeviceCode(selectList.get(i).getDeviceId());
			deviceTaskVO.setCardNo(cardNo.toString());
		smtDeviceTaskService.saveTask(deviceTaskVO);
        }
    }

    // 工作交接信息处理
    private List<SmtLeaveHandover> LeaveHandoverHandle(List<EvwJjitemRespDTO> evwJjitemList, SmtLeaveApplication leaveApplication){
        SmtLeaveHandover leaveHandover = null;
        List<SmtLeaveHandover> list = new ArrayList<>();
        EvwJjitemRespDTO evwJjite = null;
        for (int i = 0; i < evwJjitemList.size(); i++) {
            evwJjite = evwJjitemList.get(i);
            leaveHandover = new SmtLeaveHandover(
                    leaveApplication.getId(),
                    evwJjite.getEzid(),
                    evwJjite.getEmpzone(),
                    leaveApplication.getBadge(),
                    leaveApplication.getLeaveTime(),
                    evwJjite.getZrdep(),
                    evwJjite.getZrdepName(),
					evwJjite.getJjItemId(),
                    evwJjite.getJjItem(),
                    evwJjite.getJjr(),
                    evwJjite.getJjrName(),
                    LeaveHandoverConstants.FALSE,
                    LeaveHandoverConstants.FALSE,
                    LocalDateTime.now(),
                    leaveApplication.getName());
            if(Objects.nonNull(evwJjite.getJe())) {
				leaveHandover.setJe(evwJjite.getJe());
			}
			if(StrUtil.isNotEmpty(evwJjite.getJjremark())) {
				leaveHandover.setJjRemark(evwJjite.getJjremark());
			}
            list.add(leaveHandover);
        }
		return list;
    }
    // 待审核信息处理
    private void approveListHandle(Integer applicationId,String processId){
        List<SmtLeaveHandover> leaveHandoverList = this.baseMapper.getApproveList(applicationId);
        ApproveList approveList = null;
        for (SmtLeaveHandover smtLeaveHandover : leaveHandoverList) {
            approveList = new ApproveList();
            approveList.setApproveBadge(smtLeaveHandover.getJjr());
            approveList.setApproveName(smtLeaveHandover.getName()+LeaveApplicationEnum.NORMAL.getDesc()+ApproveListTypeConstants.LEAVE_TITLE);
            approveList.setApproveState(ApproveListStateEnum.PENDING.getCode());
            approveList.setApproveType(ApproveListTypeConstants.LEAVE);
            approveList.setBusinessId(processId);
            approveListService.saveApproveList(approveList);
        }
    }

    @Override
    public SmtLeaveApplication getLeaveHandoverByProcessId(String processId) {
        SmtLeaveApplication leaveApplication = leaveApplicationMapper.getLeaveApplicationByProcessId(processId);
        return leaveApplication;
    }

    @Override
    @Transactional(rollbackFor=Exception.class)
    public boolean endLeaveHandover(LeaveHandoverDTO leaveHandoverDTO) {
        SmtLeaveApplication leaveApplication = leaveApplicationMapper.getLeaveApplicationByProcessId(leaveHandoverDTO.getProcessId());
        Integer applicationId = leaveApplication.getId();
        if(ObjectUtil.isNotNull(leaveHandoverDTO) && CollectionUtil.isNotEmpty(leaveHandoverDTO.getItemList())){
            int num = this.baseMapper.endLeaveHandover(leaveHandoverDTO.getItemList(),leaveHandoverDTO.getJjr(),LeaveHandoverConstants.TRUE,DateUtil.now(),applicationId);
            if(num > 0){
                // 修改待审批状态
                ApproveList approveList = new ApproveList();
                approveList.setApproveBadge(leaveHandoverDTO.getJjr());
                approveList.setBusinessId(leaveHandoverDTO.getProcessId());
                approveList.setApproveType(ApproveListTypeConstants.LEAVE);
                approveList.setApproveState(ApproveListStateEnum.AGREE.getCode());
                approveListService.updateState(approveList);
//                List<SmtLeaveHandover> leaveHandoverList = this.baseMapper.writebackLeavehandover(applicationId);
                Integer count = this.baseMapper.getHandoverCount(applicationId, LeaveHandoverConstants.TRUE);
                if(count == 0){
	 num = leaveApplicationMapper.updateStatus(LeaveApplicationStatusEnum.END.getCode(), leaveHandoverDTO.getProcessId());
	 if(num > 0) {
			 //推送App消息
			 AppMsgPushDTO appMsgPushDTO = new AppMsgPushDTO();
			 appMsgPushDTO.setBadge(leaveApplication.getBadge());
			 appMsgPushDTO.setBussiessId(leaveApplication.getProcessId());
			 appMsgPushDTO.setTemplateCode(SmsTemplateEnum.APP_PUSH_2305.getCode());
			 appMsgPushDTO.setExtraParam("leaveStatus=" + leaveApplication.getLeaveStatus() + "||" + "approveStatus=" +  LeaveApplicationStatusEnum.END.getCode());
			 boolean flag = appMsgPushService.pushAppMsg(appMsgPushDTO);
			 log.info("{}离职工作交接完成推送结果：{}",leaveApplication.getProcessId(),flag);
	 }
                }

            }
            return num > 0;
        }
        return false;
    }

    /**
     * 工作交接完成后用户提交及离职处理
     * @param processId 流程编号
     * @return
     */
    @Override
    public boolean closeLeaveHandover(String processId) {
	SmtLeaveApplication leaveApplication = leaveApplicationMapper.getLeaveApplicationByProcessId(processId);
        Integer applicationId = leaveApplication.getId();
        int num = 0;
	if(leaveApplication.getApproveStatus().equals(LeaveApplicationStatusEnum.END.getCode())) {
	// 修改流程记录为已完成
	this.updateHandoverStatus(leaveApplication);
	//离职后的数据处理
            this.leaveHandle(applicationId);
            // 同步交接数据
            List<SmtLeaveHandover> leaveHandoverList = this.baseMapper.writebackLeavehandover(applicationId);
            boolean result = this.sysHandle(leaveHandoverList,leaveApplication.getEid());
            if(!result) {
	log.debug("工作交接同步失败：{}",leaveHandoverList);
            }
            // 修改离职申请状态
            num = leaveApplicationMapper.updateStatus(LeaveApplicationStatusEnum.COMMIT.getCode(), processId);
            // 审批通知职员
            String phoneStr = leaveApplicationMapper.getPhoneByBadge(leaveApplication.getBadge());
            String[] phones = phoneStr.split("/");
            for (String string : phones) {
                if (StrUtil.isNotBlank(string) && string.length() == 11) {
                    sendMessage(string, leaveApplication.getName(), SmsTemplateEnum.DIMISSION_3001.getCode());
                }
            }

        }
		return num > 0;
    }

    private void updateHandoverStatus(SmtLeaveApplication leaveApplicatio) {
	SmtProcessRecord processRecord = new SmtProcessRecord();
	processRecord.setStatus(NodeStatusEnum.FINISHED.getCode());
	processRecord.setRecordDate(DateUtil.date());
	smtProcessRecordService.update(processRecord, Wrappers.<SmtProcessRecord>query().lambda().eq(SmtProcessRecord::getProcessId, leaveApplicatio.getProcessId())
			.eq(SmtProcessRecord::getStaffBadge, leaveApplicatio.getBadge())
			.eq(SmtProcessRecord::getStatus, NodeStatusEnum.NOT_FINISHED.getCode()));
    }

	/**
	 * 发送短信通知
	 * @param number 手机号
	 * @param dimissioName 用户名
	 * @param tempCode 编码
	 */
	public void sendMessage(String number, String dimissioName, String tempCode) {
        //離職完成發送短信
        DimissionMsgReqDTO dimissionMsgAo = new DimissionMsgReqDTO();
        dimissionMsgAo.setNumber(number);
        dimissionMsgAo.setDimissioName(dimissioName);
        dimissionMsgAo.setTempCode(tempCode);
        Result<?> result = remoteSmsManageService.sendDimissionSms(dimissionMsgAo);
        log.debug("短消息发送结果：{}",result);
    }

    private boolean sysHandle(List<SmtLeaveHandover> leaveHandoverList,Integer eid){
        EleaveJjitemReqDTO eleaveJjitem = null;
        // 批量同步
        List<EleaveJjitemReqDTO> eleaveJjitemList = new ArrayList<>();
        for (SmtLeaveHandover leaveHandover : leaveHandoverList) {

            eleaveJjitem = new EleaveJjitemReqDTO();
            eleaveJjitem.setEzid(leaveHandover.getEzid());
            eleaveJjitem.setBadge(leaveHandover.getBadge());
            eleaveJjitem.setName(leaveHandover.getName());
            eleaveJjitem.setLeaveDate(leaveHandover.getLeaveDate());
            eleaveJjitem.setZrDep(leaveHandover.getZrdep());
            eleaveJjitem.setJjItem(leaveHandover.getJjItem());
            eleaveJjitem.setJjr(getEid(leaveHandover.getJjr()).toString());
            eleaveJjitem.setJjrName(leaveHandover.getJjRemark());
            eleaveJjitem.setQrr(getEid(leaveHandover.getQrr()).toString());
            eleaveJjitem.setQrrName(leaveHandover.getQrrName());
            eleaveJjitem.setJe(leaveHandover.getJe());
            eleaveJjitem.setJjRemark(leaveHandover.getJjRemark());
            eleaveJjitem.setJjBegin(leaveHandover.getJjBegin());
            eleaveJjitem.setJjBegintime(leaveHandover.getJjBeginTime());
            eleaveJjitem.setJjClosed(leaveHandover.getJjClosed());
            eleaveJjitem.setJjClosedTime(leaveHandover.getJjClosedTime());
            eleaveJjitem.setEid(eid);
            eleaveJjitemList.add(eleaveJjitem);
        }
        Result<Boolean> result = remoteEleaveJjitemService.save(eleaveJjitemList,SecurityConstants.FROM_IN);
        if(!result.isSuccess()) {
	throw new TCEException("同步离职交接项目异常");
        }
        return result.getData();
    }

    private Integer getEid(String badge) {
	Result<EvwEmphrYsRespDTO> result = remoteEvwEmphrYsService.info(badge, SecurityConstants.FROM_IN);
	Integer eid = 0;
		if(result.isSuccess() && ObjectUtil.isNotNull(result.getData())) {
			eid = result.getData().getEId();
		}
		return eid;
    }

    // 离职处理
    private void leaveHandle(Integer applicationId){
        SmtLeaveApplication leaveApplication = leaveApplicationMapper.selectById(applicationId);
        SmtStaff staff = this.baseMapper.getStaffByBadge(leaveApplication.getBadge());
		// 员工离职状态会自动同步 删除员工权限
		List<SmtParkBu> list = smtParkBuService.list(Wrappers.<SmtParkBu>query().lambda().eq(SmtParkBu::getCompId,staff.getCompId()));
		List<Integer> parkIds = new ArrayList<>();
		if(CollUtil.isNotEmpty(list)){
			list.forEach(bu->{
				parkIds.add(bu.getParkId());
			});
		}
        // 删除车辆
        this.deleteVehicle(staff.getId(), parkIds);
		smtIscStaffCardService.removeStaffCardsByStaffId(staff.getId());

        Logout(staff.getId(), parkIds);
        // 删除宿舍
        this.deleteRoom(staff.getId());
    }

    // 删除车辆及权限
    private void deleteVehicle(long staffId, List<Integer> parkIds){
        List<Long> vehicleIds = vehicleStaffMapper.getDeivceByStaffId(staffId);
        if(CollectionUtil.isNotEmpty(vehicleIds)) {
	 for (long vehicleId : vehicleIds) {
                 vehicleService.deleteVehicle(vehicleId,parkIds);
             }
        }
    }

    // 删除宿舍
    private void deleteRoom(long staffId){
        SmtDormitoryStaff dormitoryStaff = dormitoryStaffService.getOne(Wrappers.<SmtDormitoryStaff>query().lambda().eq(SmtDormitoryStaff::getStaffId, staffId));
        if(ObjectUtil.isNotNull(dormitoryStaff)) {
	dormitoryStaffService.removeBedById(dormitoryStaff.getId());
        }
    }

    @Override
    public List<SmtLeaveHandover> getLeaveHandover(String processId) {
        SmtLeaveApplication leaveApplication = leaveApplicationMapper.getLeaveApplicationByProcessId(processId);
        List<SmtLeaveHandover> list = this.baseMapper.getLeaveHandover(leaveApplication.getId(),null,null);
        return list;
    }

    private void saveProcessRecord(SmtLeaveApplication leaveApplication) {
	SmtProcessRecord processRecord = new SmtProcessRecord();
	processRecord.setCreatTime(DateUtil.date());
	processRecord.setNodeName("工作交接");
	processRecord.setProcessId(leaveApplication.getProcessId());
	processRecord.setStaffBadge(leaveApplication.getBadge());
	processRecord.setStaffName(leaveApplication.getName());
	processRecord.setStatus(NodeStatusEnum.NOT_FINISHED.getCode());
	processRecord.setRecordDate(DateUtil.date());
	smtProcessRecordService.save(processRecord);
	this.startLeaveHandover(leaveApplication.getId());
    }

    /**
     * 开始工作交接
     * @param applicationId
     */
    private void startLeaveHandover(Integer applicationId) {
         SmtLeaveHandover leaveHandover = new SmtLeaveHandover(applicationId, LeaveHandoverConstants.TRUE, DateUtil.date());
         this.baseMapper.startLeaveHandover(leaveHandover);
    }

    @Override
    public List<SmtLeaveHandover> getLeaveHandover(String processId,String jjr) {
        SmtLeaveApplication leaveApplication = leaveApplicationMapper.getLeaveApplicationByProcessId(processId);
        List<SmtLeaveHandover> list = this.baseMapper.getLeaveHandover(leaveApplication.getId(),jjr,LeaveHandoverConstants.TRUE);
        return list;
    }
}
