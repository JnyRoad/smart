package com.tce.smart.xcc6.core.service.impl;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.common.core.util.DateUtils;
import com.tce.smart.xcc6.core.entity.RsDpt;
import com.tce.smart.xcc6.core.entity.RsEmp;
import com.tce.smart.xcc6.core.mapper.RsXCEmpMapper;
import com.tce.smart.xcc6.core.service.IRsXCEmpService;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;

/**
 * 人事信息服务实现类
 *
 * @author mkwu
 * @date 2019-07-29
 */
@Service
public class RsXCEmpServiceImpl extends ServiceImpl<RsXCEmpMapper, RsEmp> implements IRsXCEmpService {
	@Override
	public Boolean saveEmp(String empNo, String empName, Integer empSex, String dptNo, Date grpDate,String empIDNo) {
		//通过部门编号查询部门Id
		RsDpt rsDpt = new RsDpt();
		rsDpt = rsDpt.selectOne(new LambdaQueryWrapper<RsDpt>().eq(RsDpt::getDptNo,dptNo));
		String dptSysID = rsDpt.getDptSysID();
		RsEmp rsEmp = this.baseMapper.getRsEmpByEmpNo(empNo);

		if(null != rsEmp){
			rsEmp.setEmpSexID(empSex.shortValue());
			return this.baseMapper.updateRsEmp(rsEmp);
		}

		rsEmp = new RsEmp();
		//rsEmp.setEmpSysID(empNo);
		rsEmp.setEmpNo(empNo);
		rsEmp.setEmpName(empName);
		rsEmp.setEmpSexID(empSex.shortValue());
		rsEmp.setDptSysID(dptSysID);
		rsEmp.setEmpGrpDate(grpDate);
		rsEmp.setEmpStatusID(20);    //在职
//		rsEmp.setGrdSysID("00000000-0000-0000-0000-000000000000");
//		rsEmp.setEduSysID("00000000-0000-0000-0000-000000000000");
//		rsEmp.setTitSysID("00000000-0000-0000-0000-000000000000");
//		rsEmp.setPosSysID("00000000-0000-0000-0000-000000000000");
//		rsEmp.setTypSysID("00000000-0000-0000-0000-000000000000");
//		rsEmp.setLzxSysID("00000000-0000-0000-0000-000000000000");
//		rsEmp.setLzySysID("00000000-0000-0000-0000-000000000000");
//		rsEmp.setPrvSysID("00000000-0000-0000-0000-000000000000");
//		rsEmp.setGzjSysID("00000000-0000-0000-0000-000000000000");
//		rsEmp.setGzdSysID("00000000-0000-0000-0000-000000000000");
		rsEmp.setEmpIDType(0);
		rsEmp.setCardTypeID(1);
		rsEmp.setPeriodType("A");
		rsEmp.setEmpIDNo(empIDNo);

		return this.save(rsEmp);
	}

	@Override
	public Boolean leaveEmp(String empNo) {
		RsEmp rsEmp = this.baseMapper.getRsEmpByEmpNo(empNo);

		if(null != rsEmp){
			//离职
			rsEmp.setEmpStatusID(30);
			rsEmp.setEmpLeaveDate(DateUtil.parseDateTime(DateUtil.today() + DateUtils.POSTFEX));
			return this.baseMapper.updateRsEmp(rsEmp);
		}
		return false;
	}

	@Override
	public Boolean intoEmp(String empNo) {
		RsEmp rsEmp = this.baseMapper.getRsEmpByEmpNo(empNo);

		if(null != rsEmp){
			//离职转在职
			rsEmp.setEmpStatusID(20);
			return this.baseMapper.updateRsEmp(rsEmp);
		}
		return false;
	}

	@Override
	public Map<String, Object> queryEmpPhoto(String empNo) {
		return this.baseMapper.queryEmpPhoto(empNo);
	}
}
