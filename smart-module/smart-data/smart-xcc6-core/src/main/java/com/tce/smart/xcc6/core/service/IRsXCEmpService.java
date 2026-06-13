package com.tce.smart.xcc6.core.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.xcc6.core.entity.RsEmp;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 人事信息服务类
 *
 * @author mkwu
 * @date 2019-07-29
 */
public interface IRsXCEmpService extends IService<RsEmp> {
	/**
	 * 保存Emp员工信息
	 * @param empNo
	 * @param empName
	 * @param empSex
	 * @param dptNo
	 * @param grpDate
	 * @return
	 */
	Boolean saveEmp(String empNo,String empName,Integer empSex,String dptNo,Date grpDate,String empIDNo);

	/**
	 * Emp员工离职
	 * @param empNo
	 * @return
	 */
	Boolean leaveEmp(String empNo);

	/**
	 * Emp员工离职转在职
	 * @param empNo
	 * @return
	 */
	Boolean intoEmp(String empNo);

	Map<String,Object> queryEmpPhoto(String empNo);

}
