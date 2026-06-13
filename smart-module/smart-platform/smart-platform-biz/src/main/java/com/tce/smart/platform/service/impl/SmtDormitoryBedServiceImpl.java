package com.tce.smart.platform.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.platform.api.dto.req.DormitoryBedReqDTO;
import com.tce.smart.platform.api.dto.req.dormitorymange.BedReqDTO;
import com.tce.smart.platform.api.dto.resp.dormitorymange.SearchDormitoryRoomDetailRespDTO;
import com.tce.smart.platform.api.dto.resp.staffmange.StaffRespDTO;
import com.tce.smart.platform.core.dto.DormitoryBedPageQueryDTO;
import com.tce.smart.platform.core.dto.dormitorymanage.BedDetailDTO;
import com.tce.smart.platform.core.entity.*;
import com.tce.smart.platform.core.mapper.SmtDormitoryBedMapper;
import com.tce.smart.platform.core.vo.DormitoryStaffFamilyVO;
import com.tce.smart.platform.core.vo.DormitoryStaffVO;
import com.tce.smart.platform.service.*;
import com.tce.smart.platform.service.dormitoryconfig.SmtDormitoryPersonService;
import com.tce.smart.tool.enums.DeleteStatusEnum;
import com.tce.smart.tool.enums.FamilyTypeEnum;
import com.tce.smart.tool.enums.StaffStatusEnum;
import com.tce.smart.tool.exception.TCEException;
import com.tce.smart.tool.util.RegexUtils;
import lombok.extern.slf4j.Slf4j;
import lombok.var;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 园区宿舍楼l楼层中房间的床位数
 *
 * @author 齐佩
 * @date 2019-04-13 18:17:21
 */
@Slf4j
@Service
public class SmtDormitoryBedServiceImpl extends ServiceImpl<SmtDormitoryBedMapper, SmtDormitoryBed>
		implements SmtDormitoryBedService {
	@Autowired
	private SmtDormitoryBedMapper mapper;
	@Autowired
	private SmtDormitoryStaffService domitoryStaffService;
	@Autowired
	private SmtDormitoryStaffHistoryService dormitoryStaffHistoryService;
	@Autowired
	private SmtStaffService smtStaffService;
	@Autowired
	private SmtStaffFamilyDormitoryService smtStaffFamilyDormitoryService;
	@Autowired
	private SmtDormitoryOutRemarkService smtDormitoryOutRemarkService;
	@Autowired
	private SmtDormitoryPersonService smtDormitoryPersonService;

	@Autowired
	private SmtDormitoryService smtDormitoryService;
	@Autowired
	private SmtParkService smtParkService;
	@Autowired
	private SmtDormitoryTypeService smtDormitoryTypeService;
	@Value("${smart.xc-park-id:0}")
	private Integer xcParkId;
	/**
	 * 查询床位的入住信息
	 */
	@Override
	public IPage<DormitoryStaffVO> getDormitoryBedOfStaff(Page page, DormitoryBedPageQueryDTO bed) {
		String userName = SecurityUtils.getUser().getUsername();
		List<Integer> parkIdList = smtDormitoryPersonService.getParkId(userName);
		if(CollUtil.isEmpty(parkIdList)) {
			parkIdList = SecurityUtils.getUser().getParkIdList();
		}
		bed.setDormitoryIds(smtDormitoryPersonService.getDormitoryId(userName, null));
		IPage<DormitoryStaffVO> dormitoryBedOfStaff = mapper.getDormitoryBedOfStaff(page, bed, parkIdList);
		List<DormitoryStaffVO> records = dormitoryBedOfStaff.getRecords();
		if(CollUtil.isEmpty(records)){
			return null;
		}

		Set<Integer> dorIds = records.stream().map(DormitoryStaffVO::getDormitoryId).collect(Collectors.toSet());
		List<SmtDormitory> dormitoryList = smtDormitoryService.list(new LambdaQueryWrapper<SmtDormitory>().in(SmtDormitory::getId, dorIds));
		Map<Integer, List<SmtDormitory>> dormitoryMap = dormitoryList.stream().collect(Collectors.groupingBy(SmtDormitory::getId));

		Set<Integer> parkIds = records.stream().map(DormitoryStaffVO::getParkId).collect(Collectors.toSet());
		List<SmtPark> parkList = smtParkService.list(new LambdaQueryWrapper<SmtPark>().in(SmtPark::getId, parkIds));
		Map<Integer, List<SmtPark>> parkMap = parkList.stream().collect(Collectors.groupingBy(SmtPark::getId));

		Set<Integer> dormitoryTypeIds = records.stream().map(DormitoryStaffVO::getDormitoryTypeId).collect(Collectors.toSet());
		List<SmtDormitoryType> dormitoryTypeList = smtDormitoryTypeService.list(new LambdaQueryWrapper<SmtDormitoryType>().in(SmtDormitoryType::getId, dormitoryTypeIds));
		Map<Integer, List<SmtDormitoryType>> dormitoryTypeMap = dormitoryTypeList.stream().collect(Collectors.groupingBy(SmtDormitoryType::getId));

		for (DormitoryStaffVO dormitoryStaffVO : records) {
			String badge = dormitoryStaffVO.getStaffBadge();
			if(dormitoryMap.containsKey(dormitoryStaffVO.getDormitoryId())){
				dormitoryStaffVO.setDormitoryName(dormitoryMap.get(dormitoryStaffVO.getDormitoryId()).get(0).getDormitoryName());
			}

			if(parkMap.containsKey(dormitoryStaffVO.getParkId())){
				dormitoryStaffVO.setParkName(parkMap.get(dormitoryStaffVO.getParkId()).get(0).getParkName());
			}

			if(dormitoryTypeMap.containsKey(dormitoryStaffVO.getDormitoryTypeId())){
				dormitoryStaffVO.setDormitoryTypeName(dormitoryTypeMap.get(dormitoryStaffVO.getDormitoryTypeId()).get(0).getTypeName());
			}

			if (Objects.nonNull(badge)) {
				SmtStaff smtStaff = smtStaffService.getOne(Wrappers.<SmtStaff>query().lambda().eq(SmtStaff::getBadge, badge));
				if (Objects.isNull(smtStaff)) {
					dormitoryStaffVO.setIsStaff(0);
				} else {
					dormitoryStaffVO.setPqcompany(smtStaff.getPqcompany());
					dormitoryStaffVO.setIsStaff(1);
					dormitoryStaffVO.setStatus(smtStaff.getStatus());
					dormitoryStaffVO.setJoinDate(smtStaff.getCreateTime());
					if(StaffStatusEnum.UNKNOWN.getCode().equals(smtStaff.getStatus())){
						//未知状态 不显示入职日期 离职日期 离职原因
						dormitoryStaffVO.setJoinDate(null);
						dormitoryStaffVO.setLeaDate(null);
						dormitoryStaffVO.setLeaType(null);
					}
				}
				//获得住宿备注
				dormitoryStaffVO.setRemark(smtDormitoryOutRemarkService.getNewRemark(dormitoryStaffVO.getId()));
				//获得家属信息
				List<SmtStaffFamilyDormitory> staffFamilyDormitories = smtStaffFamilyDormitoryService.list(new LambdaQueryWrapper<SmtStaffFamilyDormitory>()
						.eq(SmtStaffFamilyDormitory::getStaffBadge, badge)
						.eq(SmtStaffFamilyDormitory::getDelFlag, DeleteStatusEnum.NOT_DELETE.getCode())
				);
				if (CollUtil.isNotEmpty(staffFamilyDormitories)) {
					List<String> family = new ArrayList<>();
					for (SmtStaffFamilyDormitory dor : staffFamilyDormitories) {
						String str = FamilyTypeEnum.desc(dor.getRelation()) + ":" + dor.getName();
						family.add(str);
					}
					dormitoryStaffVO.setFamily(family);
				}
			}
		}
		dormitoryBedOfStaff.setRecords(records);
		return dormitoryBedOfStaff;
	}

	@Override
	public List<DormitoryStaffFamilyVO> getDormitoryStaffFamily(DormitoryBedPageQueryDTO bed) {
		List<Integer> parkIdList = SecurityUtils.getUser().getParkIdList();
		List<DormitoryStaffFamilyVO> dormitoryBedOfStaff = mapper.getStaffFamily(bed, parkIdList);
		for (DormitoryStaffFamilyVO dormitoryStaffVO : dormitoryBedOfStaff) {
			if (Objects.nonNull(dormitoryStaffVO.getFamilyRelation())) {
				dormitoryStaffVO.setFamilyRelationDesc(FamilyTypeEnum.desc(dormitoryStaffVO.getFamilyRelation()));
			}
		}
		return dormitoryBedOfStaff;
	}


	/**
	 * 给床位添加人员
	 */
	@Override
	public Result addDormitoryBedOfStaff(SmtDormitoryStaff smtDormitoryStaff) {
		// TODO Auto-generated method stub
		// 查询床位信息。

		return new Result<>(smtDormitoryStaff.insert());
	}

	/**
	 * 人员离职后删除床位信息
	 */
	@Override
	public Result deleteStaffBed(SmtDormitoryStaff smtDormitoryStaff) {
		// TODO Auto-generated method stub

		if (Objects.isNull(smtDormitoryStaff.getId())) {
			return new Result<>(domitoryStaffService.removeById(smtDormitoryStaff.getId()));
		}

		if (smtDormitoryStaff.getStaffBadge() != null && !smtDormitoryStaff.getStaffBadge().equals("")) {
			return new Result<>(domitoryStaffService.remove(Wrappers.<SmtDormitoryStaff>query().lambda().eq(SmtDormitoryStaff::getStaffBadge, smtDormitoryStaff.getStaffBadge())));
		}

		return new Result<>(true);

	}

	@Override
	public Result updateDormitoryBedOfStaff(SmtDormitoryStaff smtDormitoryStaff) {
		// TODO Auto-generated method stub
		// 判断工号是否包含中文或特殊字符
		String staffBadge = smtDormitoryStaff.getStaffBadge().replace(" ", "");
		if (!RegexUtils.matchBadge(staffBadge)) {
			return new Result<>(Boolean.FALSE,"工号仅允许字母和数字");
		}

		SmtDormitoryStaff byId = domitoryStaffService.getById(smtDormitoryStaff.getId());
		if (xcParkId.equals(byId.getParkId())) {
			int count = dormitoryStaffHistoryService.count(Wrappers.<SmtDormitoryStaffHistory>lambdaQuery()
					.eq(SmtDormitoryStaffHistory::getStaffBadge, staffBadge)
					.eq(SmtDormitoryStaffHistory::getParkId, xcParkId)
			);
			if (count > 0) {
				return new Result<>(Boolean.FALSE, "工号已存在");
			}
		}
		byId.setStaffBadge(staffBadge);
		byId.setStaffName(smtDormitoryStaff.getStaffName().replace(" ", ""));
		byId.setStaffSex(smtDormitoryStaff.getStaffSex());
		if (StrUtil.isNotBlank(smtDormitoryStaff.getJobName())) {
			byId.setJobName(smtDormitoryStaff.getJobName());
		}
		return new Result<>(byId.updateById());
	}

	@Override
	public Boolean updateBedName(BedReqDTO bedReqDTO) {
		List<Integer> parkList = SecurityUtils.getUser().getParkIdList();
		//查询床位信息
		SmtDormitoryBed smtDormitoryBed = this.getById(bedReqDTO.getBedId());
		if (null == smtDormitoryBed || !parkList.contains(smtDormitoryBed.getParkId())) {
			//床位不存在或者当前用户不属于该园区
			throw new TCEException("床位不存在");
		}
		SmtDormitoryBed dormitoryBed = new SmtDormitoryBed();
		dormitoryBed.setId(bedReqDTO.getBedId());
		dormitoryBed.setBedName(bedReqDTO.getBedName());
		return this.updateById(dormitoryBed);
	}

	@Transactional
    @Override
    public Boolean switchDelFlg(BedReqDTO bedReqDTO, SmtDormitoryRoomService smtDormitoryRoomService) {
		//判断床位是否存在
		SmtDormitoryBed dormitoryBed = this.getById(bedReqDTO.getBedId());
		if(null == dormitoryBed){
			throw new TCEException("床位不存在");
		}

		if(dormitoryBed.getDelFlag().equals(bedReqDTO.getDelFlag())){
			return true;
		}

		//判断床位当前是否住人
		int count = domitoryStaffService.count(new LambdaQueryWrapper<SmtDormitoryStaff>()
				.eq(SmtDormitoryStaff::getBedId, bedReqDTO.getBedId())
		);
		if(count > 0){
			throw new TCEException("床位正在使用中");
		}
		dormitoryBed.setDelFlag(bedReqDTO.getDelFlag());

		if(DeleteStatusEnum.IS_DELETE.getCode().equals(bedReqDTO.getDelFlag())){
			//删除床位 房间床位数减1
			smtDormitoryRoomService.decrementBedNum(dormitoryBed.getRoomId());
		} else if(DeleteStatusEnum.NOT_DELETE.getCode().equals(bedReqDTO.getDelFlag())){
			//床位设为可用 房间床位数加1
			smtDormitoryRoomService.incrementBedNum(dormitoryBed.getRoomId());
		}

		return this.updateById(dormitoryBed);
    }

    @Override
	public Integer getLeaveStaff(DormitoryBedPageQueryDTO bed) {
		// TODO Auto-generated method stub
		bed.setDormitoryIds(smtDormitoryPersonService.getDormitoryId(SecurityUtils.getUser().getUsername(), null));
		return this.mapper.getLeaveStaff(bed);
	}

	@Override
	public Integer getFreeBedCount(Integer parkId) {
		//List<Integer> parkIdList = SecurityUtils.getUser().getParkIdList();
		return this.baseMapper.getFreeBedCount(parkId);
	}

	@Override
	public List<SearchDormitoryRoomDetailRespDTO.BedDetail> getBedDetail(Integer roomId) {
		List<BedDetailDTO> bedDetails = this.getBaseMapper().getBedDetail(roomId);
		var respDtos = new ArrayList<SearchDormitoryRoomDetailRespDTO.BedDetail>();
		bedDetails.forEach(item -> {
			var respDto = new SearchDormitoryRoomDetailRespDTO.BedDetail();
			respDto.setBedId(item.getBedId());
			respDto.setDorStaffId(item.getDorStaffId());
			respDto.setBedNumber(item.getBedNumber());
			if (null != item.getStaffBadge()) {
				//在有入住的情况下 设置员工的入职状态
				//null-未住人, 1-在职， 2-离职未退宿， 3-未入职
				respDto.setInStatus(item.getStaffStatus() == null ? 3 : (item.getStaffStatus() == 0 ? 2 : 1));
			}
			respDto.setInTime(item.getInTime());

			var staffInfo = new StaffRespDTO();
			staffInfo.setStaffBadge(item.getStaffBadge());
			staffInfo.setStaffName(item.getStaffName());
			staffInfo.setSex(item.getStaffSex());
			staffInfo.setDepName(item.getDepName());
			staffInfo.setJobName(item.getJobName());

			respDto.setStaffInfo(staffInfo);

			respDtos.add(respDto);
		});
		return respDtos;
	}

	@Override
	public Result<List<SmtDormitoryBed>> queryBed(DormitoryBedReqDTO bed) {
		QueryWrapper<SmtDormitoryBed> queryWrapper = new QueryWrapper<>();
		queryWrapper.lambda().eq(SmtDormitoryBed::getRoomId, bed.getRoomId());
		List<SmtDormitoryBed> bedList = mapper.selectList(queryWrapper);
		return new Result<>(bedList);
	}
}
