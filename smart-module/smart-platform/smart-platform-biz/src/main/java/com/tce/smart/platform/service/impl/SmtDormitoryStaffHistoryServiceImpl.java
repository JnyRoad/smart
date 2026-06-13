package com.tce.smart.platform.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.platform.core.dto.DormitoryStatisticsDTO;
import com.tce.smart.platform.core.dto.StaffInDormitoryHistoryDTO;
import com.tce.smart.platform.core.dto.UpdateDormitoryStaffDTO;
import com.tce.smart.platform.core.entity.SmtDormitoryStaffHistory;
import com.tce.smart.platform.core.mapper.SmtDormitoryStaffHistoryMapper;
import com.tce.smart.platform.core.vo.DormitoryStaffHistoryVO;
import com.tce.smart.platform.core.vo.DormitoryStatisticsVO;
import com.tce.smart.platform.service.SmtDormitoryStaffHistoryService;
import com.tce.smart.platform.service.dormitoryconfig.SmtDormitoryPersonService;
import lombok.AllArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * 员工宿舍信息表
 *
 * @author 齐佩
 * @date 2019-04-18 14:32:40
 */
@Service
@AllArgsConstructor
public class SmtDormitoryStaffHistoryServiceImpl extends ServiceImpl<SmtDormitoryStaffHistoryMapper, SmtDormitoryStaffHistory> implements SmtDormitoryStaffHistoryService {

	private final SmtDormitoryStaffHistoryMapper mapper;

	private final SmtDormitoryPersonService smtDormitoryPersonService;

	/**
	 * 查询退宿记录
	 */
	@Override
	public Result getSmtDormitoryStaffHistory(Page page, StaffInDormitoryHistoryDTO dto, String rangTimeIn,
			String rangTimeOut) {
		// TODO Auto-generated method stub
		if(StringUtils.isNotBlank(rangTimeIn))
		{
			dto.setInStartTime(rangTimeIn.split(",")[0]);
			dto.setInEndTime(	rangTimeIn.split(",")[1]);
		}
		if(StringUtils.isNotBlank(rangTimeOut))
		{
			dto.setOutStartTime(rangTimeOut.split(",")[0]);
			dto.setOutEndTime(	rangTimeOut.split(",")[1]);
		}

		//dto.setType(DormitoryHisotryTypeEnum.IN_DORMITORY.getCode()); //排除办理入职的，查询离职，换宿，外宿的
		String userName = SecurityUtils.getUser().getUsername();
		List<Integer> parkIdList = smtDormitoryPersonService.getParkId(userName);
		if(CollUtil.isEmpty(parkIdList)) {
			parkIdList = SecurityUtils.getUser().getParkIdList();
		}
		List<Integer> dormitoryId = smtDormitoryPersonService.getDormitoryId(userName, null);
		if(CollUtil.isNotEmpty(dormitoryId)) {
			dto.setDormitoryIds(dormitoryId);
		}
		IPage<DormitoryStaffHistoryVO> pageRe = mapper.getSmtDormitoryStaffHistory(page, dto, parkIdList);
		return new Result<>(pageRe);
	}

	@Override
	public Boolean deleteDor(Integer id){
		return this.removeById(id);
	}

	@Override
	public String getByBadge(String badge) {
		List<SmtDormitoryStaffHistory> staffHistories = this.list(Wrappers.<SmtDormitoryStaffHistory>lambdaQuery()
				.eq(SmtDormitoryStaffHistory::getStaffBadge, badge));
		if (CollUtil.isNotEmpty(staffHistories)) {
			return staffHistories.get(0).getStaffName();
		}
		return StringUtils.EMPTY;
	}

	@Override
	public Result updateById(UpdateDormitoryStaffDTO updateDormitoryStaffDTO) {
		// TODO Auto-generated method stub
		String createTime=updateDormitoryStaffDTO.getCreateTime();
		  SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		  Date parse =null;
		  try {
			  parse=formatter.parse(createTime);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		  SmtDormitoryStaffHistory st=new SmtDormitoryStaffHistory();
		  st.setId(updateDormitoryStaffDTO.getId());
		  st.setTime(parse);
		  return new Result<>(st.updateById());
	}

	@Override
	public DormitoryStatisticsVO statistics(DormitoryStatisticsDTO dormitoryStatisticsDTO) {
		// TODO Auto-generated method stub
		DormitoryStatisticsVO vo=new DormitoryStatisticsVO();
		Integer count= this.baseMapper.statistics(dormitoryStatisticsDTO);
		Integer totalBed=this.baseMapper.totalBed(dormitoryStatisticsDTO);
		vo.setBedTotal(totalBed);
		vo.setInCount(count);
		vo.setInTime(dormitoryStatisticsDTO.getInTime());
		return vo;
	}

	@Override
	public IPage<DormitoryStaffHistoryVO> statisticsDetial(Page page,DormitoryStatisticsDTO dormitoryStatisticsDTO)
	{
		// TODO Auto-generated method stub

		IPage<DormitoryStaffHistoryVO> result=this.baseMapper.statisticsDetial(page,dormitoryStatisticsDTO);
		return result;
	}



}
