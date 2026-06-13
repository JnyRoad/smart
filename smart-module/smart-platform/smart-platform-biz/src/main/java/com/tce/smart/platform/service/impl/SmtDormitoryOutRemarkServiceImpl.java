package com.tce.smart.platform.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.common.core.util.BeanUtils;
import com.tce.smart.platform.api.dto.req.EditDormitoryOutRemarkReqDTO;
import com.tce.smart.platform.core.entity.SmtDormitoryOutRemark;
import com.tce.smart.platform.core.mapper.SmtDormitoryOutRemarkMapper;
import com.tce.smart.platform.service.SmtDormitoryOutRemarkService;
import com.tce.smart.tool.enums.DormitoryOutRemarkEnum;
import com.tce.smart.tool.enums.OneOrZeroEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 住宿备注表
 *
 * @author fushiping
 * @date 2019-04-13 18:19:44
 */
@Slf4j
@Service
public class SmtDormitoryOutRemarkServiceImpl extends ServiceImpl<SmtDormitoryOutRemarkMapper, SmtDormitoryOutRemark> implements SmtDormitoryOutRemarkService {

	@Override
	public List<SmtDormitoryOutRemark> getList(Integer dorStaffId) {
		return this.list(Wrappers.<SmtDormitoryOutRemark>query().lambda()
				.eq(SmtDormitoryOutRemark::getDorStaffId, dorStaffId)
				.orderByDesc(SmtDormitoryOutRemark::getCreateTime));
	}


	public List<SmtDormitoryOutRemark> getListByHistory(Integer dorHistoryStaffId) {
		return this.list(Wrappers.<SmtDormitoryOutRemark>query().lambda()
				.eq(SmtDormitoryOutRemark::getDorHistoryStaffId, dorHistoryStaffId)
				.orderByDesc(SmtDormitoryOutRemark::getCreateTime));
	}

	@Override
	public Boolean editRemark(EditDormitoryOutRemarkReqDTO remarkReqDTO) {
		SmtDormitoryOutRemark remark = BeanUtils.transform(SmtDormitoryOutRemark.class, remarkReqDTO);
		if (Objects.isNull(remarkReqDTO.getId())) {
			remark.setCreateTime(new Date());
		}
		remark.setUpdateTime(new Date());
		return this.saveOrUpdate(remark);
	}

	@Override
	public String getNewRemark(Integer dorStaffId) {
		List<SmtDormitoryOutRemark> remarks = this.getList(dorStaffId);
		if (CollUtil.isEmpty(remarks)) {
			return null;
		}
		SmtDormitoryOutRemark remark = remarks.stream().findFirst().get();
        String builder = DormitoryOutRemarkEnum.desc(remark.getReasonType()) +
                ":" +
                DateUtil.format(remark.getStartTime(), "yyyy/MM/dd") +
                "-" +
                DateUtil.format(remark.getEndTime(), "yyyy/MM/dd");
		return builder;
	}

	@Override
	public Boolean transferRemark(Integer dorStaffId, Integer newDorStaffId) {
		List<SmtDormitoryOutRemark> list = this.getList(dorStaffId);
		if(CollUtil.isEmpty(list)) {
			return Boolean.TRUE;
		}
		List<SmtDormitoryOutRemark> newList = list.stream().map(remark -> {
			remark.setDorStaffId(newDorStaffId);
			remark.setCreateTime(new Date());
			remark.setId(null);
			return remark;
		}).collect(Collectors.toList());
		return this.saveBatch(newList);
	}

	@Override
	public Boolean updateDorStaffId(Integer dorStaffId, Integer dorHistoryStaffId) {
		List<SmtDormitoryOutRemark> list = this.getList(dorStaffId);
		if(CollUtil.isEmpty(list)) {
			return Boolean.TRUE;
		}
		return this.update(Wrappers.<SmtDormitoryOutRemark>update().lambda()
				.set(SmtDormitoryOutRemark::getDorHistoryStaffId, dorHistoryStaffId)
				.set(SmtDormitoryOutRemark::getUpdateTime, new Date())
				.eq(SmtDormitoryOutRemark::getDorStaffId,  dorStaffId));
	}

	@Override
	public Integer getRemarkDate(Integer dorStaffId, Integer dorHistoryStaffId, Date startTime, Date endTime) {
		List<SmtDormitoryOutRemark> remarks = new ArrayList<>();
		if (Objects.nonNull(dorHistoryStaffId)) {
			remarks = this.getListByHistory(dorHistoryStaffId);
		}
		if (Objects.nonNull(dorStaffId)) {
			remarks = this.getList(dorStaffId);
		}
		log.info("计算备注天数，备注列表：{}", remarks);
		if (CollUtil.isEmpty(remarks)) {
			return OneOrZeroEnum.ZERO.getCode();
		}
		//计算月中的天数集合
		List<DateTime> dateTimes = DateUtil.rangeToList(DateUtil.beginOfDay(startTime), DateUtil.beginOfDay(endTime), DateField.DAY_OF_YEAR);
		//备注天数集合
		List<DateTime> remarkDate = new ArrayList<>();
		for (SmtDormitoryOutRemark remark : remarks) {
			if (remark.getEndTime().before(startTime)) {
				continue;
			}
			List<DateTime> dateTimeRemark = DateUtil.rangeToList(DateUtil.beginOfDay(remark.getStartTime()),
					DateUtil.beginOfDay(remark.getEndTime()), DateField.DAY_OF_YEAR);
			remarkDate.addAll(dateTimeRemark);
		}
		//备注天数去重
		remarkDate.stream().distinct().collect(Collectors.toList());
		log.info("计算天数：{}，备注天数：{}", dateTimes, remarkDate);
		//计算天数与备注天数交集
		dateTimes.retainAll(remarkDate);
		return dateTimes.size();
	}

//	@Override
//	public Integer getRemarkDate1(Integer dorStaffId, Integer dorHistoryStaffId, String startTime1, String endTime1) throws ParseException {
//		List<SmtDormitoryOutRemark> remarks = new ArrayList<>();
//		if (Objects.nonNull(dorHistoryStaffId)) {
//			remarks = this.getListByHistory(dorHistoryStaffId);
//		}
//		if (Objects.nonNull(dorStaffId)) {
//			remarks = this.getList(dorStaffId);
//		}
//		if (CollUtil.isEmpty(remarks)) {
//			return OneOrZeroEnum.ZERO.getCode();
//		}
//		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//		Date endTime = formatter.parse(endTime1);
//		Date startTime = formatter.parse(startTime1);
//		//计算月中的天数集合
//		List<DateTime> dateTimes = DateUtil.rangeToList(startTime, endTime,DateField.DAY_OF_YEAR);
//		//备注天数集合
//		List<DateTime> remarkDate = new ArrayList<>();
//		for (SmtDormitoryOutRemark remark : remarks) {
//			if (remark.getEndTime().before(startTime)) {
//				continue;
//			}
//			List<DateTime> dateTimeRemark = DateUtil.rangeToList(remark.getStartTime(),
//					remark.getEndTime(), DateField.DAY_OF_YEAR);
//			remarkDate.addAll(dateTimeRemark);
//		}
//		//备注天数去重
//		remarkDate.stream().distinct().collect(Collectors.toList());
//		//计算天数与备注天数交集
//		dateTimes.retainAll(remarkDate);
//		return dateTimes.size();
//	}
}
