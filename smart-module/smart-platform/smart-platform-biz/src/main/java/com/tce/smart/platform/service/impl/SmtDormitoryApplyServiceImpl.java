package com.tce.smart.platform.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.platform.api.dto.req.dormitorymange.DormitoryApplyFailBackDTO;
import com.tce.smart.platform.api.dto.req.dormitorymange.DormitoryApplyReqDTO;
import com.tce.smart.platform.api.dto.req.dormitorymange.DormitoryApplySearchReqDTO;
import com.tce.smart.platform.api.dto.req.dormitorymange.DormitoryDistReqDTO;
import com.tce.smart.platform.api.dto.resp.dormitorymange.DormitoryApplySearchRespDTO;
import com.tce.smart.platform.api.dto.resp.dormitorymange.DormitoryDistRespDTO;
import com.tce.smart.platform.core.dto.dormitorymanage.DormitoryApplyDTO;
import com.tce.smart.platform.core.entity.SmtDormitoryApply;
import com.tce.smart.platform.core.entity.SmtStaff;
import com.tce.smart.platform.core.mapper.SmtDormitoryApplyMapper;
import com.tce.smart.platform.core.mapper.SmtStaffMapper;
import com.tce.smart.platform.service.SmtDormitoryApplyService;
import com.tce.smart.platform.service.SmtDormitoryRoomService;
import com.tce.smart.platform.service.SmtDormitoryStaffService;
import com.tce.smart.platform.service.dormitoryconfig.SmtDormitoryPersonService;
import com.tce.smart.tool.enums.BedTypeEnum;
import com.tce.smart.tool.enums.DormitoryApplyStatusEnum;
import com.tce.smart.tool.exception.TCEException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @description: SmtDormitoryApplyServiceImpl
 * @date: 2020/12/29 15:38
 * @author: wuling
 * @version: 1.0
 */
@Slf4j
@Service
public class SmtDormitoryApplyServiceImpl extends ServiceImpl<SmtDormitoryApplyMapper, SmtDormitoryApply> implements SmtDormitoryApplyService {

	@Resource
	private SmtStaffMapper smtStaffMapper;

	@Resource
	private SmtDormitoryRoomService smtDormitoryRoomService;

	@Autowired
	private SmtDormitoryPersonService smtDormitoryPersonService;

	@Override
	public IPage<DormitoryApplySearchRespDTO> getApplyRecord(DormitoryApplySearchReqDTO searchReqDTO) {
		Page searchPage = new Page(searchReqDTO.getCurrent(),searchReqDTO.getSize());
		DormitoryApplyDTO applyDTO = new DormitoryApplyDTO();
		List<Integer> parkIdList = smtDormitoryPersonService.getParkId(SecurityUtils.getUser().getUsername());
		if(CollUtil.isEmpty(parkIdList)) {
			parkIdList = SecurityUtils.getUser().getParkIdList();
		}
		applyDTO.setParkList(parkIdList);
		IPage<DormitoryApplyDTO> applyRecord = this.baseMapper.getApplyRecord(searchPage, applyDTO);
		Page resPage = new Page(searchPage.getCurrent(),searchPage.getSize(),searchPage.getTotal());
		List<DormitoryApplySearchRespDTO> respDTOList = new ArrayList<>();
		applyRecord.getRecords().forEach(item -> {
			DormitoryApplySearchRespDTO searchRespDTO = new DormitoryApplySearchRespDTO();
			BeanUtil.copyProperties(item,searchRespDTO);
			searchRespDTO.setLikeTypeDesc(BedTypeEnum.desc(searchRespDTO.getLikeType()));
			respDTOList.add(searchRespDTO);
		});
		resPage.setRecords(respDTOList);
		return resPage;
	}

	@Override
	public Boolean saveApply(DormitoryApplyReqDTO applyReqDTO) {
		String staffBadge = SecurityUtils.getUser().getUsername();

		//查询员工信息
		List<SmtStaff> staffInfo = smtStaffMapper.selectList(new LambdaQueryWrapper<SmtStaff>()
				.eq(SmtStaff::getBadge,staffBadge)
		);

		//添加申请记录
		SmtDormitoryApply smtDormitoryApply = new SmtDormitoryApply();
		smtDormitoryApply.setParkId(applyReqDTO.getParkId());
		smtDormitoryApply.setLikeType(applyReqDTO.getBedType());
		smtDormitoryApply.setApplyRemark(applyReqDTO.getApplyRemark());
		smtDormitoryApply.setStaffBadge(staffBadge);
		smtDormitoryApply.setStaffName(staffInfo.get(0).getName());
		smtDormitoryApply.setStatus(DormitoryApplyStatusEnum.APPLYING.getCode());
		smtDormitoryApply.setCreateTime(new Date());
		return this.save(smtDormitoryApply);
	}

	@Override
	public Boolean cancelApply() {
		String staffBadge = SecurityUtils.getUser().getUsername();
		SmtDormitoryApply dormitoryApply = this.getOne(new LambdaQueryWrapper<SmtDormitoryApply>()
				.eq(SmtDormitoryApply::getStaffBadge, staffBadge)
				.eq(SmtDormitoryApply::getStatus, DormitoryApplyStatusEnum.APPLYING.getCode())
		);
		if(null == dormitoryApply){
			throw new TCEException("不存在申请中的记录");
		}
		dormitoryApply.setStatus(DormitoryApplyStatusEnum.CANCEL.getCode());
		return this.updateById(dormitoryApply);
	}

	@Override
	public Boolean failbackApply(DormitoryApplyFailBackDTO failBackDTO) {
		//查询申请记录
		SmtDormitoryApply dormitoryApply = this.getById(failBackDTO.getId());
		if(null == dormitoryApply){
			throw new TCEException("不存在申请记录");
		} else if (!DormitoryApplyStatusEnum.APPLYING.getCode().equals(dormitoryApply.getStatus())){
			throw new TCEException("非申请中，不能退回");
		}
		dormitoryApply.setStatus(DormitoryApplyStatusEnum.FAILBACK.getCode());
		dormitoryApply.setResultRemark(failBackDTO.getResultRemark());
		dormitoryApply.setUpdateTime(new Date());
		return this.updateById(dormitoryApply);
	}

	@Override
	public Boolean manualDis(Long applyId, Integer bedId, SmtDormitoryStaffService smtDormitoryStaffService) {
		//查询申请记录
		SmtDormitoryApply dormitoryApply = this.getById(applyId);
		if(null == dormitoryApply){
			throw new TCEException("申请记录不存在");
		} else if(!DormitoryApplyStatusEnum.APPLYING.getCode().equals(dormitoryApply.getStatus())){
			throw new TCEException("非申请中,不能操作");
		}
		//添加入住记录
		smtDormitoryStaffService.addDormitoryStaff(dormitoryApply.getStaffBadge(),bedId);
		//修改申请记录
		dormitoryApply.setStatus(DormitoryApplyStatusEnum.SUCCESS.getCode());
		dormitoryApply.setUpdateTime(new Date());

		return this.updateById(dormitoryApply);
	}

	@Override
	public DormitoryDistRespDTO recommendDis(Long applyId) {
		//查询申请记录
		SmtDormitoryApply dormitoryApply = this.getById(applyId);
		if(null == dormitoryApply){
			throw new TCEException("申请记录不存在");
		} else if(!DormitoryApplyStatusEnum.APPLYING.getCode().equals(dormitoryApply.getStatus())){
			throw new TCEException("非申请中,不能操作");
		}
		DormitoryDistRespDTO distRespDTO = smtDormitoryRoomService.recommendBed(DormitoryDistReqDTO.builder()
				.parkId(dormitoryApply.getParkId())
				.staffBadge(dormitoryApply.getStaffBadge())
				.bedType(dormitoryApply.getLikeType())
				.build());
		if(null == distRespDTO){
			throw new TCEException("没有找到合适的床位");
		}
		return distRespDTO;
	}
}
