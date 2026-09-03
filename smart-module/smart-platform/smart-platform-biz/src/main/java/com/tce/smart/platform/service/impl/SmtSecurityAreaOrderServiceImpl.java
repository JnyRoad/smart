package com.tce.smart.platform.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.common.core.constant.CommonConstants;
import com.tce.smart.common.core.exception.SmartException;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.util.BeanUtils;
import com.tce.smart.common.core.util.CollectionUtils;
import com.tce.smart.common.core.util.StringUtils;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.data.api.dto.msg.req.SecurityAreaVisitDetailReqDTO;
import com.tce.smart.data.api.dto.msg.req.SecurityAreaVisitMainReqDTO;
import com.tce.smart.data.api.dto.msg.req.SendSecurityAreaVisitReqDTO;
import com.tce.smart.data.api.feign.msg.RemoteOaWorkFlowService;
import com.tce.smart.platform.api.dto.req.securityarea.SmtSecurityAreaOrderReqDTO;
import com.tce.smart.platform.api.dto.req.securityarea.SmtVisitListReqDTO;
import com.tce.smart.platform.api.dto.resp.FlowRespDTO;
import com.tce.smart.platform.api.dto.resp.securityarea.SecurityAreaOrderDetailDTO;
import com.tce.smart.platform.api.dto.resp.securityarea.SecurityAreaOrderListDTO;
import com.tce.smart.platform.core.dto.WorkFlowLogDTO;
import com.tce.smart.platform.core.dto.WorkFlowLogDataDTO;
import com.tce.smart.platform.core.dto.securityarea.SecurityAreaOrderDTO;
import com.tce.smart.platform.core.entity.SmtSecurityAreaSupplier;
import com.tce.smart.platform.core.entity.SmtStaff;
import com.tce.smart.platform.core.entity.securityarea.SmtSecurityAreaOrder;
import com.tce.smart.platform.core.entity.securityarea.SmtVisitList;
import com.tce.smart.platform.core.mapper.SmtSecurityAreaOrderMapper;
import com.tce.smart.platform.core.service.SmtImageService;
import com.tce.smart.platform.service.*;
import com.tce.smart.tool.enums.ApplicationEnum;
import com.tce.smart.tool.enums.SecurityAreaVisitStatusEnum;
import com.tce.smart.tool.enums.SmtImageEnum;
import com.tce.smart.tool.exception.TCEException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @description: SmtSecurityAreaOrderServiceImpl
 * @date: 2020-07-30 9:13
 * @author: wuling
 * @version: 1.0
 */
@Slf4j
@Service
public class SmtSecurityAreaOrderServiceImpl extends ServiceImpl<SmtSecurityAreaOrderMapper, SmtSecurityAreaOrder> implements SmtSecurityAreaOrderService {

	@Value("${security-area.order.addition-link:}")
	private String securityAreaAadditionLink;

	@Resource
	private SmtVisitListService smtVisitListService;

	@Resource
	private SmtStaffService smtStaffService;

	@Resource
	private SmtSecurityAreaSupplierService smtSecurityAreaSupplierService;

	@Resource
	private RemoteOaWorkFlowService remoteOaWorkFlowService;

	@Resource
	private ImageService imageService;

	@Resource
	private SmtImageService smtImageService;

	@Resource
	private IOAWorkflowService oaWorkflowService;


	@Transactional(rollbackFor = Exception.class)
	@Override
	public boolean saveOrder(SmtSecurityAreaOrderReqDTO smtSecurityAreaOrderReqDTO) {
		String staffBadge = SecurityUtils.getUser().getUsername();
		//保密区预约记录添加流程
		//这里先保存预约记录是为了防止并发添加 因为数据库里面不能加唯一索引
		//1. 先添加预约记录到本地库	记录OA流程ID先设为0
		SmtSecurityAreaOrder smtSecurityAreaOrder = new SmtSecurityAreaOrder();
		BeanUtils.copyProperties(smtSecurityAreaOrderReqDTO, smtSecurityAreaOrder);
		smtSecurityAreaOrder.setProcessId("0");
		smtSecurityAreaOrder.setStaffBadge(staffBadge);
		smtSecurityAreaOrder.setStatus(SecurityAreaVisitStatusEnum.APPLY.getCode());
		smtSecurityAreaOrder.setCarryGoods(smtSecurityAreaOrderReqDTO.getCarryGoods());

		if (StringUtils.isNotEmpty(smtSecurityAreaOrderReqDTO.getAdditionalContent())) {
			//如果存在附件   存储在图片表中
			String imageCode = smtImageService.saveImage(0, smtSecurityAreaOrderReqDTO.getAdditionalContent(), SmtImageEnum.SECURITY_AREA_ORDER_CON.getCode());
			smtSecurityAreaOrder.setAdditionalName(imageCode);
		}

		smtSecurityAreaOrder.setCreateTime(new Date());
		this.save(smtSecurityAreaOrder);

		//2. 判断该供应商当前是否存在已申请状态的记录 即未审批的记录 存在则不能申请 流程结束
		int count = this.count(new QueryWrapper<SmtSecurityAreaOrder>().lambda()
				.eq(SmtSecurityAreaOrder::getSupplierId, smtSecurityAreaOrderReqDTO.getSupplierId())
				.eq(SmtSecurityAreaOrder::getStaffBadge,staffBadge)
				.eq(SmtSecurityAreaOrder::getComeTime, smtSecurityAreaOrderReqDTO.getComeTime())
				.eq(SmtSecurityAreaOrder::getStatus, SecurityAreaVisitStatusEnum.APPLY.getCode())
		);
		//包含本条 判断大于1
		if (count > 1) {
			//不能重复提交 事务回滚
			log.error("保密区预约申请添加记录失败，不能重复申请，{}", JSONUtil.toJsonStr(smtSecurityAreaOrderReqDTO));
			throw new TCEException("不能重复申请");
		}

		//3. 提交到OA系统 失败则流程结束
		String processId = getProcessId(smtSecurityAreaOrder, smtSecurityAreaOrderReqDTO);

		if (StringUtils.isEmpty(processId)) {
			//提交OA系统失败 事务回滚
			log.error("保密区预约申请提交OA失败，{}", JSONUtil.toJsonStr(smtSecurityAreaOrderReqDTO));
			throw new TCEException("提交OA失败");
		}

		//4. 更新OA流程ID
		this.updateById(SmtSecurityAreaOrder.builder().id(smtSecurityAreaOrder.getId()).processId(processId).build());

		//5. 保存来访人员名单
		List<SmtVisitList> visitLists = new ArrayList<>();
		for (SmtVisitListReqDTO smtVisitListReqDTO : smtSecurityAreaOrderReqDTO.getVisitListReqDTOS()) {
			SmtVisitList smtVisitList = new SmtVisitList();
			BeanUtils.copyProperties(smtVisitListReqDTO, smtVisitList);
			smtVisitList.setOrderId(smtSecurityAreaOrder.getId());
			visitLists.add(smtVisitList);
		}
		smtVisitListService.saveBatch(visitLists);

		return true;
	}

	@Override
	public IPage<SecurityAreaOrderListDTO> getOrderListByUser(Page page) {
		String staffBadge = SecurityUtils.getUser().getUsername();
		String staffName = smtStaffService.getOne(new QueryWrapper<SmtStaff>().lambda().eq(SmtStaff::getBadge, staffBadge)).getName();
		//这里不写泛型的原因是 用SecurityAreaOrderListDTO对象替换SmtSecurityAreaOrder对象
		IPage page1 = this.page(page, new QueryWrapper<SmtSecurityAreaOrder>().lambda().eq(SmtSecurityAreaOrder::getStaffBadge, staffBadge).orderByDesc(SmtSecurityAreaOrder::getCreateTime));
		List<SecurityAreaOrderListDTO> securityAreaOrderListDTOS = new ArrayList<>();
		for (Object obj : page1.getRecords()) {
			SmtSecurityAreaOrder order = (SmtSecurityAreaOrder) obj;
			SecurityAreaOrderListDTO securityAreaOrderListDTO = new SecurityAreaOrderListDTO();
			BeanUtils.copyProperties(order, securityAreaOrderListDTO);
			//获取状态描述
			securityAreaOrderListDTO.setStatusDesc(SecurityAreaVisitStatusEnum.desc(order.getStatus()));
			securityAreaOrderListDTO.setApplicant(staffName);
			securityAreaOrderListDTOS.add(securityAreaOrderListDTO);
		}
		page1.setRecords(securityAreaOrderListDTOS);
		return page1;
	}

	@Override
	public SecurityAreaOrderDetailDTO getOrderDetail(Long id) {
		SecurityAreaOrderDTO securityAreaOrderDetail = this.baseMapper.getSecurityAreaOrderDetail(id);

		SecurityAreaOrderDetailDTO securityAreaOrderDetailDTO = new SecurityAreaOrderDetailDTO();
		BeanUtils.copyProperties(securityAreaOrderDetail, securityAreaOrderDetailDTO);
		securityAreaOrderDetailDTO.setSupplier(securityAreaOrderDetail.getSupplierName());

		//查询来访人员列表
		List<SmtVisitList> list = smtVisitListService.list(new QueryWrapper<SmtVisitList>().lambda().eq(SmtVisitList::getOrderId, id));
		List<SmtVisitListReqDTO> visitListReqDTOS = new ArrayList<>();
		for (SmtVisitList smtVisitList : list) {
			SmtVisitListReqDTO smtVisitListReqDTO = new SmtVisitListReqDTO();
			BeanUtils.copyProperties(smtVisitList, smtVisitListReqDTO);
			visitListReqDTOS.add(smtVisitListReqDTO);
		}
		securityAreaOrderDetailDTO.setVisitListReqDTOS(visitListReqDTOS);
		//状态描述
		securityAreaOrderDetailDTO.setStatusDesc(SecurityAreaVisitStatusEnum.desc(securityAreaOrderDetail.getStatus()));

		//附件访问地址
		if (StringUtils.isNotEmpty(securityAreaOrderDetail.getAdditionalName())) {
			securityAreaOrderDetailDTO.setAdditionalLink(imageService.buildImageUrl(securityAreaOrderDetail.getAdditionalName()));
		}

		//获取OA审批流程
		List<FlowRespDTO> flowList = new ArrayList<>();
		getOAProcessFlow(securityAreaOrderDetail.getProcessId(),flowList);

		securityAreaOrderDetailDTO.setFlowList(flowList);

		return securityAreaOrderDetailDTO;
	}

	public void getOAProcessFlow(String processId,List<FlowRespDTO> list) {
		WorkFlowLogDTO workFlowLogDTO = oaWorkflowService.query(processId);
		if(ObjectUtil.isNotNull(workFlowLogDTO) && workFlowLogDTO.success()) {
			List<WorkFlowLogDataDTO> flowRecords = workFlowLogDTO.getResultdata();
			if(CollectionUtils.isNotEmpty(flowRecords)){
				flowRecords.forEach(flowRecord->getProcessRecord(list, flowRecord));
			}
		}
	}

	private void getProcessRecord(List<FlowRespDTO> list,WorkFlowLogDataDTO process) {
		FlowRespDTO flowVO = new FlowRespDTO ();
		if(StrUtil.isEmpty(process.getNODENAME())) {
			flowVO.setNodeName("");
		}else {
			String[] nodeNames = process.getNODENAME().split(" ");
			if(nodeNames.length == 2) {
				flowVO.setNodeName(nodeNames[1]);
			}
		}
		String dateTime = process.getOPERATEDATE() + " " + process.getOPERATETIME();
		if(StrUtil.isNotBlank(process.getOPERATEDATE()) && StrUtil.isNotBlank(process.getOPERATETIME())) {
			flowVO.setProcessDate(DateUtil.parse(dateTime, "yyyy-MM-dd HH:mm:ss"));
		}
		flowVO.setProcessDesc(ApplicationEnum.desc(process.getLOGTYPE()));
		flowVO.setRemark(process.getREMARK()==null?"":process.getREMARK());
		flowVO.setCreateUser(process.getLASTNAME()==null?"":process.getLASTNAME());
		list.add(flowVO);
	}

	/**
	 * 获取保密区预约 OA 审批流程 ID，并确认供应商尚未被逻辑删除。
	 */
	private String getProcessId(SmtSecurityAreaOrder smtSecurityAreaOrder, SmtSecurityAreaOrderReqDTO smtSecurityAreaOrderReqDTO) {
		String processId = "";

		//保密区预约记录
		SecurityAreaVisitMainReqDTO securityAreaVisitMainReqDTO = new SecurityAreaVisitMainReqDTO();

		//获取员工信息
		SmtStaff smtStaff = smtStaffService.getOne(Wrappers.<SmtStaff>query().lambda().eq(SmtStaff::getBadge, smtSecurityAreaOrder.getStaffBadge()));
		//获取供应商信息
		SmtSecurityAreaSupplier smtSecurityAreaSupplier = smtSecurityAreaSupplierService.getById(smtSecurityAreaOrder.getSupplierId());
		if (smtSecurityAreaSupplier == null) {
			throw new TCEException("供应商记录不存在或已删除");
		}

		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

		securityAreaVisitMainReqDTO.setLiucbh(smtSecurityAreaOrder.getId().toString());
		securityAreaVisitMainReqDTO.setLaifdw(smtSecurityAreaSupplier.getCompanyName());
		securityAreaVisitMainReqDTO.setLaifqy(smtSecurityAreaOrder.getVisitArea());
		securityAreaVisitMainReqDTO.setLaifrq(simpleDateFormat.format(smtSecurityAreaOrder.getComeTime()));
		securityAreaVisitMainReqDTO.setXiedwf(smtSecurityAreaOrder.getCarryGoods());
		securityAreaVisitMainReqDTO.setLaifsy(smtSecurityAreaOrder.getVisitType());
		securityAreaVisitMainReqDTO.setBmxy(0);
		securityAreaVisitMainReqDTO.setSfz(smtSecurityAreaOrder.getInterviewName());
		securityAreaVisitMainReqDTO.setLxdh(Long.parseLong(smtSecurityAreaOrder.getInterviewPhone()));
		securityAreaVisitMainReqDTO.setPtz(smtSecurityAreaOrder.getEscortName());
		securityAreaVisitMainReqDTO.setLxdh1(Long.parseLong(smtSecurityAreaOrder.getEscortPhone()));
		securityAreaVisitMainReqDTO.setBeiz(smtSecurityAreaOrder.getRemark());
		securityAreaVisitMainReqDTO.setShenqr(smtStaff.getBadge());
		securityAreaVisitMainReqDTO.setShifjq(0);
		securityAreaVisitMainReqDTO.setShenqrbm(smtStaff.getCompName());
		securityAreaVisitMainReqDTO.setSfsy(0);
		securityAreaVisitMainReqDTO.setLaixdh2(smtSecurityAreaOrder.getInterviewPhone());
		securityAreaVisitMainReqDTO.setLaixdh3(smtSecurityAreaOrder.getEscortPhone());
		securityAreaVisitMainReqDTO.setLaifsj("");
		securityAreaVisitMainReqDTO.setLkrq(simpleDateFormat.format(smtSecurityAreaOrder.getLeaveTime()));
		securityAreaVisitMainReqDTO.setLksj("");

		//设置随访人员
		List<SecurityAreaVisitDetailReqDTO> detailReqDTOList = new ArrayList<>();
		if (null != smtSecurityAreaOrderReqDTO.getVisitListReqDTOS()) {
			smtSecurityAreaOrderReqDTO.getVisitListReqDTOS().forEach(item -> {
				SecurityAreaVisitDetailReqDTO visiter = new SecurityAreaVisitDetailReqDTO();
				visiter.setXingm(item.getVisitName());
				//visiter.setPhone(item.getPhone());
				visiter.setShenfz(item.getVisitCardId());
				visiter.setZjrlh(0);
				visiter.setDjzt(1);
				detailReqDTOList.add(visiter);
			});
		}

		securityAreaVisitMainReqDTO.setBadge(smtStaff.getBadge());
		securityAreaVisitMainReqDTO.setName(smtStaff.getName());
		securityAreaVisitMainReqDTO.setCompid(smtStaff.getCompId());
		securityAreaVisitMainReqDTO.setDepid(smtStaff.getDepId());
		securityAreaVisitMainReqDTO.setJobid(smtStaff.getJobId());


		SendSecurityAreaVisitReqDTO sendSecurityAreaVisitReqDTO = new SendSecurityAreaVisitReqDTO();
		sendSecurityAreaVisitReqDTO.setSecurityAreaVisitMainReqDTO(securityAreaVisitMainReqDTO);
		sendSecurityAreaVisitReqDTO.setSecurityAreaVisitDetailReqDTOs(detailReqDTOList);

		//获取保密区预约OA流程id
		log.info("获取保密区预约OA流程id，param=({})", securityAreaVisitMainReqDTO);
		Result<String> result = remoteOaWorkFlowService.sendSecurityAreaVisit(sendSecurityAreaVisitReqDTO);
		log.info("获取保密区预约OA流程id，result=({})", result);
		if (CommonConstants.SUCCESS == result.getCode()) {
			if (ObjectUtil.isNotNull(result.getData())) {
				processId = result.getData();
				if(StrUtil.isEmpty(processId)) {
					throw new TCEException("发起OA审批流程失败");
				}
				if("-7".equals(processId)){
					throw new TCEException("获取不到OA审批人员，请联系OA管理处理后再试");
				}
			} else {
				this.removeById(smtSecurityAreaOrder.getId());
				throw new TCEException("OA流程提交异常");
			}
		} else {
			this.removeById(smtSecurityAreaOrder.getId());
			throw new TCEException("申请失败，请重新操作");

		}
		return processId;
	}

}
